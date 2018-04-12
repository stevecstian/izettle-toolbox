package com.izettle.data.migrator.cassandra.datastax;

public class CassandraDataStaxMigratorParameters {

    private final int batchSize;
    private final int maxThreads;
    private final int rowsToVerifyPerBatch;

    public CassandraDataStaxMigratorParameters(
        final int batchSize,
        final int maxThreads,
        final int rowsToVerifyPerBatch
    ) {
        this.batchSize = batchSize;
        this.maxThreads = maxThreads;
        this.rowsToVerifyPerBatch = rowsToVerifyPerBatch;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public int getRowsToVerifyPerBatch() {
        return rowsToVerifyPerBatch;
    }

}
