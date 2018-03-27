package com.izettle.data.migrator.dropwizard;

import com.google.common.collect.ImmutableMultimap;
import com.izettle.data.migrator.core.BatchDataMigrator;
import io.dropwizard.servlets.tasks.Task;
import java.io.PrintWriter;
import java.util.List;

/**
 * Dropwizard Task to return the progress of a running migration.
 */
public class DataMigratorProgressTask extends Task {

    private final BatchDataMigratorTask migratorTask;

    public DataMigratorProgressTask(BatchDataMigratorTask migratorTask) {
        super("migration-progress");
        this.migratorTask = migratorTask;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter out)
        throws Exception {
        out.println("Data Migrator status:");

        List<BatchDataMigrator<?, ?>> migrators = migratorTask.getCurrentMigrators();
        out.println("=========================================================");
        out.println("Number of batches being currently processed: " + BatchDataMigrator.CONCURRENT_BATCHES_COUNT.get());
        out.println("Number of migrators currently active: " + migrators.size());
        migrators.forEach(m -> out.println(" * " + m.getName() + " is" + (m.isRunning() ? " " : " not ") + "running"));
        out.println("=========================================================");
    }
}
