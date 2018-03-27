package com.izettle.data.migrator.core;

public final class MigrationThrottlingParameters {

    private static final int NEVER_SLEEP = Integer.MAX_VALUE;

    public static final MigrationThrottlingParameters DEFAULT =
        new MigrationThrottlingParameters(NEVER_SLEEP, -1L);

    private final int batchesBetweenSleeps;
    private final long sleepInMillis;

    public MigrationThrottlingParameters(final int batchesBetweenSleeps, final long sleepInMillis) {

        // must be non-zero, positive (as this will be used in division via the module operator)!
        this.batchesBetweenSleeps = batchesBetweenSleeps < 1 ? NEVER_SLEEP : batchesBetweenSleeps;

        // must be 0 or positive
        this.sleepInMillis = Math.max(0L, sleepInMillis);
    }

    public int getBatchesBetweenSleeps() {
        return batchesBetweenSleeps;
    }

    public long getSleepInMillis() {
        return sleepInMillis;
    }

}
