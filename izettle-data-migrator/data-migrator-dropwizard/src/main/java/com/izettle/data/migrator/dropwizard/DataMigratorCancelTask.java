package com.izettle.data.migrator.dropwizard;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import java.io.PrintWriter;

/**
 * Dropwizard Task to cancel a data migration.
 */
public class DataMigratorCancelTask extends Task {

    private final BatchDataMigratorTask migratorTask;

    public DataMigratorCancelTask(BatchDataMigratorTask migratorTask) {
        super("cancel-migration");
        this.migratorTask = migratorTask;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter out)
        throws Exception {
        out.println("Cancelling migrator task");
        migratorTask.cancel(out);
    }
}
