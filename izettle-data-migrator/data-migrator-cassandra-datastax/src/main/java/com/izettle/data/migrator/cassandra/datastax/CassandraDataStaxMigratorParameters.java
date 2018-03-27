package com.izettle.data.migrator.cassandra.datastax;

import com.datastax.driver.core.Session;

public class CassandraDataStaxMigratorParameters {

    private final Session cassandraSession;
    private final int batchSize;
    private final int maxThreads;
    private final int rowsToVerifyPerBatch;

    public CassandraDataStaxMigratorParameters(
        final Session cassandraSession,
        final int batchSize,
        final int maxThreads,
        final int rowsToVerifyPerBatch
    ) {
        this.cassandraSession = cassandraSession;
        this.batchSize = batchSize;
        this.maxThreads = maxThreads;
        this.rowsToVerifyPerBatch = rowsToVerifyPerBatch;
    }

    public Session getCassandraSession() {
        return cassandraSession;
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
