package com.izettle.data.migrator.core;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.Test;

public class BatchDataMigratorTest {

    @Test
    public void migratorRunsToCompletion() {
        AstyanaxSimulatedBatchDao<String> dao = new AstyanaxSimulatedBatchDao<>(asList(
            "a", "b", "c", "d", "e", "f", "g"
        ));
        TestBatchMigrator migrator = new TestBatchMigrator(dao, 2, 3);

        // run a full migration: this should span 3 Threads, processing batches of size 2
        migrator.runFullMigration();

        // verify that the batches were processed as expected, in any order
        assertThat(new HashSet<>(dao.getMigratedRecords())).isEqualTo(new HashSet<>(asList(
            asList("a", "b"), asList("c", "d"), asList("e", "f"), singletonList("g"))
        ));
    }

    @Test
    public void migratorCanMigrateLargeAmountsOfRecordsInManyThreads() {
        final int totalRecords = 100_000;
        final int batchSize = 100;

        List<String> records = IntStream.range(0, totalRecords)
            .mapToObj(Integer::toString)
            .collect(toList());

        AstyanaxSimulatedBatchDao<String> dao = new AstyanaxSimulatedBatchDao<>(records);

        // batches of 100 records, processed in 20 Threads
        TestBatchMigrator migrator = new TestBatchMigrator(dao, batchSize, 20);

        migrator.runFullMigration();

        List<List<String>> expectedBatches = new ArrayList<>(totalRecords / batchSize);

        for (int i = 0; i < totalRecords; i += batchSize) {
            expectedBatches.add(IntStream.range(i, i + batchSize)
                .mapToObj(Integer::toString)
                .collect(toList()));
        }

        // verify that the batches were processed as expected, in any order
        assertThat(new HashSet<>(dao.getMigratedRecords()))
            .isEqualTo(new HashSet<>(expectedBatches));
    }

    @Test
    public void migratorRunsToCompletionEvenIfErrorOccursOnExecutingMigrationStatement() {
        // this DAO will report the first batch items as having already been migrated
        // so that when the first batch fails to be migrated, it will not be attempted item-by-item
        AstyanaxSimulatedBatchDao<String> dao = new AstyanaxSimulatedBatchDao<>(asList(
            "a", "b", "c", "d", "e", "f"
        ), new HashSet<>(Arrays.asList("a", "b")), 1);

        // this migrator will throw an error on the first execution
        TestBatchMigrator migrator = new TestBatchMigrator(dao, 2, 1, 0, null, null);

        // run a full migration: this should span 1 Thread, processing batches of size 2
        migrator.runFullMigration();

        // verify that the batches were processed as expected, in any order
        // the first batch is always dropped
        assertThat(new HashSet<>(dao.getMigratedRecords())).isEqualTo(new HashSet<>(asList(
            asList("c", "d"), asList("e", "f"))
        ));
    }

    @Test
    public void migratorRunsToCompletionEvenIfErrorOccursOnExecutingMigrationStatementWithRecovery() {
        // this DAO will never report an item as having already been migrated,
        // therefore the item-by-item batch error recovery procedure will recover the failed batch
        AstyanaxSimulatedBatchDao<String> dao = new AstyanaxSimulatedBatchDao<>(asList(
            "a", "b", "c", "d", "e", "f"
        ));

        // this migrator will throw an error on the first execution
        TestBatchMigrator migrator = new TestBatchMigrator(dao, 2, 2, 0, null, null);

        // run a full migration: this should span 2 Threads, processing batches of size 2
        migrator.runFullMigration();

        // verify that the batches were processed as expected, in any order
        // the first batch initially fails, but then the item-by-item migration recovers them
        assertThat(new HashSet<>(dao.getMigratedRecords())).isEqualTo(new HashSet<>(asList(
            singletonList("a"), singletonList("b"), asList("c", "d"), asList("e", "f"))
        ));
    }

    @Test
    public void migratorCanBeCancelled() throws InterruptedException {
        AstyanaxSimulatedBatchDao<String> dao = new AstyanaxSimulatedBatchDao<>(asList(
            "a", "b", "c", "d", "e", "f", "g"
        ));

        // migrator uses batch size of 2, 3 Threads and a delay per migrated row
        TestBatchMigrator migrator = new TestBatchMigrator(dao, 2, 3, -1, Duration.ofMillis(4), null);

        // run a full migration asynchronously
        new Thread(migrator::runFullMigration).start();

        // wait 5ms, which should allow 2 rows to be migrated because the 2 Threads can only do two
        // rows every 4ms, then cancel the migration.
        // Notice that because it might take a few ms for the call to cancel() to be reached, there's no
        // guarantee that no more than 2 rows will be migrated... so we assume that cancel() can be called
        // within 8ms of starting the migrator Thread, which lets us assert that at most 4 rows are migrated.
        Thread.sleep(5L);
        migrator.cancel();

        // verify that at most 4 rows were processed (we can never guarantee that any Thread actually
        // starts within 5ms, so we need to be tolerant and allow 0)
        assertThat(dao.getMigratedRecords().size()).isBetween(0, 4);
    }

    @Test
    public void migratorDetectsBadMigrationAndAborts() {
        AstyanaxSimulatedBatchDao<String> dao = new AstyanaxSimulatedBatchDao<>(asList(
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j"
        ), emptySet(), 1);

        // migrator uses batch size of 2, 1 Thread and reports the "c" item as not having been migrated correctly,
        // which should cause the whole migration to be aborted.
        // The migration should be forcibly cancelled, automatically, due to the problematic row,
        // resulting in only the first batch not being migrated.
        TestBatchMigrator migrator = new TestBatchMigrator(dao, 2, 1, -1, Duration.ofMillis(0L), "c");

        // run a full migration: this should span 1 Thread, processing batches of size 2
        // and should be cancelled automatically once the second batch is processed.
        migrator.runFullMigration();

        // verify that only the first 2 batches were migrated (the last of which fails and cancels the migration)
        //noinspection unchecked
        assertThat(new HashSet<>(dao.getMigratedRecords())).isEqualTo(
            new HashSet<>(asList(asList("a", "b"), asList("c", "d"))));
    }

    private static class TestBatchMigrator
        extends BatchDataMigrator<String, List<String>> {

        private final AstyanaxSimulatedBatchDao<String> dao;
        private final int executeIndexToThrowError;
        private final Duration delayPerRow;
        private final String itemToReportAsFailedToMigrate;
        private final AtomicInteger executeIndex = new AtomicInteger();

        TestBatchMigrator(
            AstyanaxSimulatedBatchDao<String> dao,
            int batchSize,
            int maxThreads
        ) {
            this(dao, batchSize, maxThreads, -1, null, null);
        }

        TestBatchMigrator(
            AstyanaxSimulatedBatchDao<String> dao,
            int batchSize,
            int maxThreads,
            int executeIndexToThrowError,
            Duration delayPerRow,
            String itemToReportAsFailedToMigrate
        ) {
            super("test-migrator", dao, batchSize, maxThreads);
            this.dao = dao;
            this.executeIndexToThrowError = executeIndexToThrowError;
            this.delayPerRow = delayPerRow;
            this.itemToReportAsFailedToMigrate = itemToReportAsFailedToMigrate;
        }

        @Override
        public Optional<List<String>> transform(List<String> from) {
            return Optional.ofNullable(from);
        }

        @Override
        public void execute(List<String> records) throws InterruptedException {
            if (executeIndex.getAndIncrement() == executeIndexToThrowError) {
                throw new RuntimeException("EXECUTE ERROR");
            }
            if (delayPerRow != null) {
                Thread.sleep(delayPerRow.toMillis() * records.size());
            }
            dao.accept(records);
        }

        @Override
        public boolean isMigrated(List<String> from) {
            if (itemToReportAsFailedToMigrate != null) {
                return !from.contains(itemToReportAsFailedToMigrate);
            }

            // report all items as having been migrated correctly
            return true;
        }

        @Override
        protected void migrateSingleItem(String from) throws InterruptedException {
            dao.accept(singletonList(from));
        }
    }

}
