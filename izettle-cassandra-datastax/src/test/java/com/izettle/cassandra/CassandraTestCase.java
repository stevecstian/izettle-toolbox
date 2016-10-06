package com.izettle.cassandra;

import java.io.IOException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class CassandraTestCase {

    @BeforeClass
    public static void beforeClass() throws InterruptedException, IOException, TTransportException {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
    }

    @Before
    public void before() {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }
}
