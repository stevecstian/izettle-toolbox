package com.izettle.dropwizard.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.ConstantSpeculativeExecutionPolicy;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.ExponentialReconnectionPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.extras.codecs.jdk8.InstantCodec;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.setup.Environment;
import java.util.Set;
import org.hibernate.validator.constraints.NotEmpty;

public class CassandraSessionFactory {
    @NotEmpty
    private Set<String> contactPoints;

    private int port = 9042;

    @NotEmpty
    private String keySpace;

    @JsonProperty
    public Set<String> getContactPoints() {
        return contactPoints;
    }

    @JsonProperty
    public void setContactPoints(Set<String> contactPoints) {
        this.contactPoints = contactPoints;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public String getKeySpace() {
        return keySpace;
    }

    @JsonProperty
    public void setKeySpace(String keySpace) {
        this.keySpace = keySpace;
    }

    public CassandraSessionManaged build(Environment environment) {
        PoolingOptions poolingOptions = new PoolingOptions();
        poolingOptions.setConnectionsPerHost(HostDistance.LOCAL,  6, 10)
            .setConnectionsPerHost(HostDistance.REMOTE, 2, 4);
        final Cluster cluster = Cluster
            .builder()
            .withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.ONE))
            .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
            .withReconnectionPolicy(new ExponentialReconnectionPolicy(10L, 1000L))
            .withLoadBalancingPolicy(new TokenAwarePolicy(DCAwareRoundRobinPolicy.builder().build()))
            .addContactPoints(getContactPoints().stream().toArray(String[]::new))
            .withPort(getPort())
            .withSpeculativeExecutionPolicy(new ConstantSpeculativeExecutionPolicy(1000, 2))
            .withPoolingOptions(poolingOptions)
            .build();

        cluster.getConfiguration().getCodecRegistry()
            .register(InstantCodec.instance);

        Session session = cluster.connect(getKeySpace());

        CassandraSessionManaged cassandraSessionManaged = new CassandraSessionManaged(cluster, session);
        environment.lifecycle().manage(cassandraSessionManaged);

        return cassandraSessionManaged;
    }
}
