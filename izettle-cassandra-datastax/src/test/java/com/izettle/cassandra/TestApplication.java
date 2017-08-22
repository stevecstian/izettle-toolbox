package com.izettle.cassandra;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class TestApplication extends Application<TestConfiguration> {
    private CassandraSessionManaged cassandraSessionManaged = null;

    public CassandraSessionManaged getCassandraSessionManaged() {
        return cassandraSessionManaged;
    }

    @Override
    public void run(TestConfiguration configuration, Environment environment)
        throws Exception {
        this.cassandraSessionManaged = configuration.getCassandraSessionFactory().build(environment);
    }
}
