package com.izettle.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import io.dropwizard.lifecycle.Managed;

public class CassandraSessionManaged implements Managed {
    private final Cluster cluster;
    private final Session session;

    public CassandraSessionManaged(Cluster cluster, Session session) {
        this.cluster = cluster;
        this.session = session;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public Session getSession() {
        return session;
    }

    @Override
    public void start() {
        // do nothing
    }

    @Override
    public void stop() {
        cluster.closeAsync();
    }
}
