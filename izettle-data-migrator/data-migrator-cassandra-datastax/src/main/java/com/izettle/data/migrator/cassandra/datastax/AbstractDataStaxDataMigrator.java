package com.izettle.data.migrator.cassandra.datastax;

import static java.util.stream.Collectors.toList;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.izettle.data.migrator.core.BatchDataMigrator;
import com.izettle.data.migrator.core.BatchSupportedDao;
import com.izettle.data.migrator.core.DataMigrator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for {@link DataMigrator} that migrate Object to a Cassandra database using the
 * DataStax driver.
 *
 * This implementation expects the user to provide a POJO of type {@link T} for migration.
 * It does not specify how the POJO is obtained from the old database.
 *
 * @param <T> type of object to be migrated
 */
public abstract class AbstractDataStaxDataMigrator<T>
    implements DataMigrator<T, BoundStatement> {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final Session session;

    public AbstractDataStaxDataMigrator(Session session) {
        this.session = session;
    }

    /**
     * Adapt this DataMigrator into a {@link BatchDataMigrator}.
     * <p/>
     * This allows any implementation of a simple {@link AbstractDataStaxDataMigrator} to support batch
     * migrations automatically.
     *
     * @param dao DAO where records to be migrated can be fetched from
     * @param parameters batch migrator parameters
     * @return a batch data migrator adapted from this simple migrator
     */
    public BatchDataMigrator<T, List<BoundStatement>> toBatchDataMigrator(
        BatchSupportedDao<T> dao,
        CassandraDataStaxMigratorParameters parameters
    ) {
        return new BatchDataMigrator<T, List<BoundStatement>>(getClass().getSimpleName(),
            dao, parameters.getBatchSize(), parameters.getMaxThreads()
        ) {
            @Override
            public Optional<List<BoundStatement>> transform(List<T> batch) {
                return Optional.of(batch.stream()
                    .map(AbstractDataStaxDataMigrator.this::transform)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(toList()));
            }

            @Override
            public void execute(List<BoundStatement> boundStatements) {
                List<ResultSetFuture> futures = new ArrayList<>(boundStatements.size());
                for (BoundStatement statement : boundStatements) {
                    ResultSetFuture future = session.executeAsync(statement);
                    futures.add(future);
                }

                // wait for all statements to be processed
                for (ResultSetFuture future : futures) {
                    future.getUninterruptibly();
                }
            }

            @Override
            protected void migrateSingleItem(T from) throws InterruptedException {
                // use the enveloping, single-item migration - do not use migrate() as it runs asynchronously
                AbstractDataStaxDataMigrator.this.transform(from).ifPresent(AbstractDataStaxDataMigrator.this::execute);
            }

            @Override
            public boolean isMigrated(List<T> batch) {
                if (parameters.getRowsToVerifyPerBatch() < 1) {
                    return true;
                }

                // randomly select rows from the batch and check only those
                List<T> randomOrderBatch = new ArrayList<>(batch);
                Collections.shuffle(randomOrderBatch);
                return randomOrderBatch.stream()
                    .limit(parameters.getRowsToVerifyPerBatch())
                    .allMatch(AbstractDataStaxDataMigrator.this::isMigrated);
            }
        };
    }

    @Override
    public void execute(BoundStatement statement) {
        session.execute(statement);
    }

    @Override
    public void executeAsync(BoundStatement statement) {
        Futures.addCallback(
            session.executeAsync(statement),
            new FutureCallback<ResultSet>() {
                @Override
                public void onSuccess(ResultSet rows) {
                    log.debug("Successfully migrated a record");
                }

                @Override
                public void onFailure(Throwable throwable) {
                    log.error("Failed to execute statement to migrate a record", throwable);
                    log.error("Failed statement: {}", statement);
                }
            }
        );
    }

}
