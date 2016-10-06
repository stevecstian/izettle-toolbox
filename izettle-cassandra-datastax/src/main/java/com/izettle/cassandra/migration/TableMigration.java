package com.izettle.cassandra.migration;

import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;

import com.datastax.driver.core.CodecRegistry;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PagingState;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TableMigration<S, D> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MigrationConfig migrationConfig;
    private final MappingManager mappingManager;
    private final Mapper<S> sourceEntityMapper;
    private final Mapper<D> destinationEntityMapper;
    private final Statement findAll;
    private final MigrationStatus migrationStatus;
    private final Mapper<MigrationStatus> migrationStatusMapper;

    @SuppressWarnings("unchecked")
    public TableMigration(MigrationConfig migrationConfig, MappingManager mappingManager) {
        this.migrationConfig = requireNonNull(migrationConfig, "migrationConfig must not be null");
        this.mappingManager = requireNonNull(mappingManager, "mappingManager must not be null");
        sourceEntityMapper = (Mapper<S>) mappingManager.mapper(migrationConfig.getSourceEntity());
        destinationEntityMapper = (Mapper<D>) mappingManager.mapper(migrationConfig.getDestinationEntity());
        destinationEntityMapper.setDefaultSaveOptions(Mapper.Option.consistencyLevel(ConsistencyLevel.ALL));
        migrationStatusMapper = mappingManager.mapper(MigrationStatus.class);

        final CodecRegistry codecRegistry =
            mappingManager.getSession().getCluster().getConfiguration().getCodecRegistry();
        final List<TypeCodec<?>> codecs = migrationConfig.getCodecs();
        codecs.forEach(codecRegistry::register);
        this.migrationStatus = setMigrationStatus();
        migrationStatusMapper.save(migrationStatus);
        findAll = createFindAllStatement(migrationConfig);

    }

    private MigrationStatus setMigrationStatus() {

        //Find out if first time running this job or we are resuming.
        final MigrationStatus persistedMigrationStatus = migrationStatusMapper.get(migrationConfig.getName());
        if (persistedMigrationStatus != null) {
            return persistedMigrationStatus;
        }
        return new MigrationStatus(migrationConfig);

    }

    private static Statement createFindAllStatement(MigrationConfig config) {
        final Statement statement = new SimpleStatement("select * from " + config.getSourceTableName());
        statement.setConsistencyLevel(ConsistencyLevel.ONE);
        statement.setFetchSize(config.getFetchSize());
        statement.setIdempotent(true);
        statement.disableTracing();
        return statement;
    }

    @SuppressWarnings("unchecked")
    public MigrationResult execute() {

        final long start = currentTimeMillis();

        //Start with settings state (if present)
        migrationStatus.getPagingState().ifPresent(findAll::setPagingState);

        //Execute query (select *).
        final ResultSet rs = session().execute(findAll);


        //Map result.
        final Result<S> wrappedPurchases = sourceEntityMapper.map(rs);
        final int available = wrappedPurchases.getAvailableWithoutFetching();

        //If no result, we're done!
        if (available == 0) {
            return handleCompleted();
        }

        //Iterate the source
        final Iterator<S> iterator = wrappedPurchases.iterator();
        final List<D> destinationEntities = new ArrayList<>();
        do {
            destinationEntities.add((D) migrationConfig.getSourceToDestinationMapper().apply(iterator.next()));
        } while (iterator.hasNext() && destinationEntities.size() < migrationConfig.getFetchSize());

        if (migrationStatus.isReadOnly()) {
            logger.info("In read-only mode. Successfully read and mapped {} entities", destinationEntities.size());
        } else {
            logger.info(
                "Successfully mapped {} entities. About to save to: {} ",
                destinationEntities.size(), this.migrationStatus.getDestinationTableName());
            destinationEntities.forEach(destinationEntityMapper::save);
        }
        final PagingState pagingState = rs.getExecutionInfo().getPagingState();

        return MigrationResult.createCompleted();
    }

    private MigrationResult handleCompleted() {
        this.migrationStatus.setCompleted();
        this.migrationStatusMapper.save(migrationStatus);
        return MigrationResult.createCompleted();
    }

    private Session session() {
        return mappingManager.getSession();
    }

}
