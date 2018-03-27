package com.izettle.data.migrator.core;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Specialization of {@link DataMigrator} for batch migrations.
 *
 * Given a DAO that implements {@link BatchSupportedDao}, implementations of this class are able to execute the
 * statement given by {@code ToExecute}, which should batch migrate records of type {@code FromData}, in multiple
 * Threads.
 *
 * The process may be cancelled at any time by calling the {@link BatchDataMigrator#cancel()} method. This will
 * forcibly kill all Threads and completely abort the migration process as soon as possible.
 *
 * Once the {@link BatchDataMigrator#cancel()} method is called, instances of this class must no longer be used.
 *
 * @param <FromData> type of objects to be migrated
 * @param <ToExecute> action that migrates a batch of objects
 */
public abstract class BatchDataMigrator<FromData, ToExecute>
    implements DataMigrator<List<FromData>, ToExecute> {

    private static final int MAX_UNRECOVERABLE_ERRORS = 10;

    protected final Logger log;

    protected final String name;
    protected final BatchSupportedDao<FromData> dao;
    protected final int batchSize;
    protected final int maxThreads;

    private final LinkedBlockingDeque<List<FromData>> batchQueue;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final ExecutorService executorService;
    private final AtomicInteger unrecoverableErrorCount = new AtomicInteger();
    private final AtomicReference<Runnable> onCancelCallback = new AtomicReference<>();
    private final AtomicReference<MigrationThrottlingParameters> throttlingParameters
        = new AtomicReference<>(MigrationThrottlingParameters.DEFAULT);

    public static final AtomicInteger CONCURRENT_BATCHES_COUNT = new AtomicInteger();

    /**
     * Create a {@link BatchDataMigrator} instance.
     *
     * @param name of this data migrator (for debugging and metrics purposes)
     * @param dao the DAO that provides the records to be migrated
     * @param batchSize size of batches to process
     * @param maxThreads number of Threads to use to process batches
     */
    public BatchDataMigrator(
        String name,
        BatchSupportedDao<FromData> dao,
        int batchSize,
        int maxThreads
    ) {
        this.name = name;
        this.dao = dao;
        this.batchSize = batchSize;
        this.maxThreads = maxThreads;
        this.batchQueue = new LinkedBlockingDeque<>(maxThreads);
        this.log = LoggerFactory.getLogger(name);

        AtomicInteger threadIndex = new AtomicInteger();

        this.executorService = Executors.newFixedThreadPool(
            maxThreads,
            runnable -> new Thread(runnable, "batch-database-migrator-" + threadIndex.getAndIncrement())
        );
    }

    public String getName() {
        return name;
    }

    public boolean isRunning() {
        return running.get();
    }

    public void setOnCancelCallback(Runnable callback) {
        onCancelCallback.set(callback);
    }

    /**
     * Migrate single item.
     *
     * @param from to migrate
     */
    protected abstract void migrateSingleItem(FromData from) throws InterruptedException;

    @Override
    public void executeAsync(ToExecute toExecute) {
        throw new UnsupportedOperationException("Cannot run batch statement asynchronously");
    }

    @Override
    public void migrate(List<FromData> from) throws InterruptedException {
        Optional<ToExecute> statement = transform(from);
        if (statement.isPresent()) {
            // the default migrate( ) implementation runs async, override for batches to run synchronously
            execute(statement.get());
        }
    }

    private void verify(List<FromData> batch) {
        boolean isBatchMigrated = isMigrated(batch);

        if (!isBatchMigrated) {
            log.error("Detected a batch with rows that did not get migrated correctly, aborting migration");
            cancel();
        }
    }

    /**
     * Run a full database migration.
     * <p/>
     * This method may start several Threads to perform the migration, but once it returns,
     * it is guaranteed that all Threads will have completed.
     * <p/>
     * Calling this method more than once has no effect.
     * <p/>
     * To stop the migration process, call the {@link BatchDataMigrator#cancel()} method.
     */
    public final void runFullMigration() {
        if (!cancelled.get() && running.compareAndSet(false, true)) {
            log.info("Starting full database migration using {} threads", maxThreads);

            // start all workers at once, so they can start consuming the batch queue immediately
            for (int i = 0; i < maxThreads; i++) {
                executorService.submit(new Worker());
            }

            final AtomicInteger totalBatchesProcessed = new AtomicInteger(0);

            try {
                dao.forEachBatch(batchSize, batch -> {
                    boolean batchWasAccepted = false;
                    CONCURRENT_BATCHES_COUNT.incrementAndGet();
                    try {
                        while (!batchWasAccepted) {
                            try {
                                // it's necessary to only try to insert the batch into the queue if the migration has
                                // not been cancelled, otherwise, if the queue is full, this could block.
                                if (cancelled.get()) {
                                    return false;
                                }

                                // waiting if necessary for the queue to make space available...
                                // this applies "back-pressure" in our consumer, so we don't load the whole database
                                // into memory in case the writers are too much slower than this reader
                                batchWasAccepted = batchQueue.offer(batch, 1, TimeUnit.SECONDS);
                            } catch (InterruptedException e) {
                                // this can only happen if our Thread was interrupted, so stop the migration in such
                                // case
                                log.warn("Main database migrator Thread has been interrupted! Stopping migration.");
                                cancel();
                                return false;
                            }
                        }

                        final int totalBatches = totalBatchesProcessed.incrementAndGet();
                        final MigrationThrottlingParameters throttlingParams = throttlingParameters.get();

                        // throttle if needed
                        if (throttlingParams.getSleepInMillis() > 0L &&
                            (totalBatches % throttlingParams.getBatchesBetweenSleeps()) == 0) {
                            try {
                                log.info("Throttling current batch {}. Sleeping for {} ms", totalBatches,
                                    throttlingParams.getSleepInMillis()
                                );
                                Thread.sleep(throttlingParams.getSleepInMillis());
                            } catch (InterruptedException e) {
                                log.warn("Interrupted while throttling data migration batches");
                                cancel();
                            }
                        }
                    } finally {
                        CONCURRENT_BATCHES_COUNT.decrementAndGet();
                    }
                    return !cancelled.get(); // i.e. continue processing more batches unless cancelled
                });
            } catch (Exception e) {
                log.error("Problem running database migration", e);
                cancel();
            }

            // by now, all batches have been processed, stop the executor and wait for all workers to exit
            executorService.shutdownNow();

            try {
                executorService.awaitTermination(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for all workers to stop", e);
            } finally {
                running.set(false);
            }

            if (cancelled.get()) {
                log.info("DATABASE MIGRATION ABORTED!");
            } else {
                log.info("DATABASE MIGRATION COMPLETE!");
            }

        } else if (cancelled.get()) {
            throw new IllegalStateException("Cannot re-start migration after it was cancelled");
        }
    }

    /**
     * When a batch fails to be migrated, the likely reason is that one or more of the records have already been
     * migrated via an external process. To recover from such cases and make sure we don't miss any record, we
     * attempt to migrate one-by-one, first checking if the item has NOT been migrated.
     * <p/>
     * If an item has already been migrated, no re-attempt is made to migrate the record.
     * <p/>
     * If an error happens even after it has been established that a record has not been migrated, then it's logged
     * an an ERROR so that it's possible to later manually find the record and try to fix it.
     *
     * @param batch to migrate one-by-one
     * @return true to continue, false if the migration has been cancelled
     */
    private boolean attemptToMigrateOneByOne(List<FromData> batch) {
        log.info("Attempting to migrate batch rows one-by-one due to problem inserting full batch at once");
        for (FromData item : batch) {
            if (cancelled.get()) {
                return false;
            }
            boolean alreadyMigrated;
            try {
                alreadyMigrated = dao.exists(item);
            } catch (Exception e) {
                log.error("Unable to verify whether item had already been migrated: {}\nReason: {}", item, e);
                int totalErrors = unrecoverableErrorCount.incrementAndGet();

                if (totalErrors > MAX_UNRECOVERABLE_ERRORS) {
                    log.error("Too many unrecoverable errors, aborting database migration!");
                    cancel();
                    return false;
                }
                continue;
            }
            if (!alreadyMigrated) {
                try {
                    migrateSingleItem(item);
                } catch (InterruptedException e) {
                    log.warn("Interrupted while trying to migrate single item");
                    return false;
                } catch (Exception e) {
                    log.error("Unable to migrate individual item after all attempts failed: {}", item, e);
                    unrecoverableErrorCount.incrementAndGet();
                }
            } else {
                log.debug("Not migrating item as it already exists: {}", item);
            }
        }

        return true;
    }

    /**
     * Cancel the database migration.
     * <p/>
     * Notice that after calling this method, this migrator cannot be re-started.
     */
    public final void cancel() {
        if (!cancelled.getAndSet(true)) {
            log.warn("Cancelling database migration");
            cancelled.set(true);
            dao.stopMigration();
            Runnable callback = onCancelCallback.getAndSet(null);
            if (callback != null) {
                callback.run();
            }
        }
    }

    public void setThrottlingParameters(final MigrationThrottlingParameters parameters) {
        this.throttlingParameters.set(parameters);
    }

    enum QueuingOption {
        EXIT_WHEN_QUEUE_IS_EMPTY,
        CONTINUE_EVEN_WITH_EMPTY_QUEUE
    }

    private class Worker implements Runnable {

        @Override
        public void run() {
            log.info("Migrator Thread '{}' starting!", Thread.currentThread().getName());

            try {
                boolean isCancelled = consumeQueue(QueuingOption.CONTINUE_EVEN_WITH_EMPTY_QUEUE);
                if (isCancelled) {
                    log.info("Worker Thread '{}' cancelled", Thread.currentThread().getName());
                    return; // don't log success at the end!
                }
            } catch (InterruptedException e) {
                // this is expected at the end of the migration, start loop again, but exit if queue is empty
                try {
                    log.info("Migrator Thread switching to queue draining mode: {}", Thread.currentThread().getName());
                    consumeQueue(QueuingOption.EXIT_WHEN_QUEUE_IS_EMPTY);
                } catch (InterruptedException e2) {
                    log.warn("Unexpectedly interrupted worker while trying to drain the queue");
                }
            } catch (Throwable t) {
                log.error("Migrator Thread '{}' cannot continue due to error", Thread.currentThread().getName(), t);
                return;
            }

            log.info("Migrator Thread '{}' has finished its job!", Thread.currentThread().getName());
        }

        /**
         * Consume the batch queue.
         *
         * @param queuingOption queuing option
         * @return true if the worker has been cancelled, false otherwise
         * @throws InterruptedException if the thread is interrupted
         */
        @SuppressWarnings("ConstantConditions") // "stopConsumingQueue" constant improves readability a little bit
        private boolean consumeQueue(QueuingOption queuingOption) throws InterruptedException {
            List<FromData> batch;

            boolean stopConsumingQueue = true;

            while (true) {
                if (BatchDataMigrator.this.cancelled.get()) {
                    log.info("Cancelled migrator Thread '{}'", Thread.currentThread().getName());
                    return stopConsumingQueue;
                }

                switch (queuingOption) {
                    case EXIT_WHEN_QUEUE_IS_EMPTY:
                        batch = batchQueue.poll();
                        if (batch == null) { // this means the queue has no elements!
                            return !stopConsumingQueue;
                        }
                        break;
                    case CONTINUE_EVEN_WITH_EMPTY_QUEUE:
                        batch = batchQueue.poll(5, TimeUnit.SECONDS);
                        break;
                    default:
                        throw new IllegalStateException("Missing enum case");
                }

                if (batch != null && !BatchDataMigrator.this.cancelled.get()) {
                    try {
                        migrate(batch);
                    } catch (InterruptedException e) {
                        log.info("Thread Interrupted while migrating a batch");
                    } catch (Exception e) {
                        // INFO level because this is expected when a row has already been migrated
                        log.warn("There was an error migrating a batch", e);

                        boolean toContinue = attemptToMigrateOneByOne(batch);
                        if (!toContinue) {
                            log.info("Cancelled migrator Thread '{}'", Thread.currentThread().getName());
                            return stopConsumingQueue;
                        }
                    } finally {
                        if (unrecoverableErrorCount.get() > MAX_UNRECOVERABLE_ERRORS) {
                            log.error("Too many unrecoverable errors, aborting database migration!");
                            cancel();
                        } else if (!BatchDataMigrator.this.cancelled.get()) {
                            verify(batch);
                        }
                    }
                }
            }
        }

    }

}
