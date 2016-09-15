package com.izettle.dropwizard.cassandra;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.junit.Test;
import org.mockito.Mockito;

public class CassandraSessionManagedTest {
    @Test
    public void closeOnStop() {
        Cluster cluster = mock(Cluster.class);
        Session session = mock(Session.class);

        CassandraSessionManaged cassandraSessionManaged = new CassandraSessionManaged(cluster, session);
        cassandraSessionManaged.stop();
        verify(cluster, times(1)).closeAsync();
    }
}
