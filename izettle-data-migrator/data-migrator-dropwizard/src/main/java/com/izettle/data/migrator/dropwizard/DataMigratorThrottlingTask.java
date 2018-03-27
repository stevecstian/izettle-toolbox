package com.izettle.data.migrator.dropwizard;

import com.google.common.collect.ImmutableMultimap;
import com.izettle.data.migrator.core.MigrationThrottlingParameters;
import io.dropwizard.servlets.tasks.Task;
import java.io.PrintWriter;

/**
 * Dropwizard task to set throttling on the main {@link BatchDataMigratorTask}.
 */
public class DataMigratorThrottlingTask extends Task {

    private final BatchDataMigratorTask migratorTask;

    public DataMigratorThrottlingTask(final BatchDataMigratorTask migratorTask) {
        super("throttle-migration");
        this.migratorTask = migratorTask;
    }

    @Override
    public void execute(
        final ImmutableMultimap<String, String> parameters,
        final PrintWriter out
    ) {
        final int batchesBetweenSleeps, sleepInMillis;

        try {
            batchesBetweenSleeps = Integer.parseInt(parameters.get("batchesBetweenSleeps").iterator().next());
            sleepInMillis = Integer.parseInt(parameters.get("sleepInMillis").iterator().next());
        } catch (Exception e) {
            out.println("ERROR (invalid argument): " + e);
            return;
        }

        migratorTask.setThrottling(new MigrationThrottlingParameters(batchesBetweenSleeps, sleepInMillis));
    }

}
