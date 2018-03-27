package com.izettle.data.migrator.core;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * This is a test class that tries to simulate as closely as possible the beahaviour of a
 * DAO that is based on the Cassandra Astyanax driver.
 * <p/>
 * For example, the default implementation of {@link AstyanaxSimulatedBatchDao#forEachBatch(int, Function)}
 * simulates an Astyanax DAO connecting to 3 Cassandra nodes, therefore using 3 Threads, one for each node,
 * to run the for-each callaback to process batches.
 *
 * @param <DTO>
 */
public class AstyanaxSimulatedBatchDao<DTO> implements BatchSupportedDao<DTO> {

    private final List<DTO> oldRecords;
    private final Set<DTO> dtosToReportAlreadyMigrated;
    private final List<List<DTO>> migratedRecords = Collections.synchronizedList(new ArrayList<>());
    private final int concurrencyLevel;

    public AstyanaxSimulatedBatchDao(
        List<DTO> oldRecords,
        Set<DTO> dtosToReportAlreadyMigrated,
        int concurrencyLevel
    ) {
        if (concurrencyLevel < 1) {
            throw new IllegalArgumentException("Concurrency level must be at least 1");
        }

        this.oldRecords = Collections.synchronizedList(new ArrayList<>(oldRecords));
        this.dtosToReportAlreadyMigrated = dtosToReportAlreadyMigrated;
        this.concurrencyLevel = concurrencyLevel;
    }

    public AstyanaxSimulatedBatchDao(List<DTO> oldRecords) {
        this(oldRecords, emptySet(), 3);
    }

    @Override
    public boolean exists(DTO s) {
        return dtosToReportAlreadyMigrated.contains(s);
    }

    @Override
    public void forEachBatch(
        int batchSize, Function<List<DTO>, Boolean> consumer
    ) {
        // simulate a real DAO's multi-Threaded for-each implementation
        Iterator<DTO> iterator = oldRecords.iterator();

        List<Thread> threads = IntStream.range(0, concurrencyLevel)
            .mapToObj(i -> new Thread(createForEachPublisher(iterator, batchSize, consumer)))
            .collect(toList());

        threads.forEach(Thread::start);

        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Runnable createForEachPublisher(
        Iterator<DTO> iterator,
        int batchSize,
        Function<List<DTO>, Boolean> consumer
    ) {
        return () -> {
            boolean done = false;

            while (!done) {
                List<DTO> batch = new ArrayList<>(batchSize);

                synchronized (iterator) {
                    for (int i = 0; i < batchSize; i++) {
                        if (iterator.hasNext()) {
                            batch.add(iterator.next());
                        } else {
                            break;
                        }
                    }
                }

                if (batch.isEmpty()) {
                    done = true;
                } else {
                    boolean toContinue = consumer.apply(batch);

                    done = !iterator.hasNext() || !toContinue;
                }
            }
        };
    }

    public void accept(List<DTO> records) {
        migratedRecords.add(records);
    }

    public List<DTO> getOldRecords() {
        return oldRecords;
    }

    public List<List<DTO>> getMigratedRecords() {
        return new ArrayList<>(migratedRecords);
    }

    @Override
    public void stopMigration() {
        // nothing to do
    }

}
