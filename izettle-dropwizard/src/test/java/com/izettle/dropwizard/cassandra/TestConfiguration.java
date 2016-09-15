package com.izettle.dropwizard.cassandra;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class TestConfiguration extends Configuration {
    @Valid
    @NotNull
    private CassandraSessionFactory cassandraSessionFactory = new CassandraSessionFactory();

    @JsonProperty("cassandra")
    public CassandraSessionFactory getCassandraSessionFactory() {
        return cassandraSessionFactory;
    }

    @JsonProperty("cassandra")
    public void setCassandraSessionFactory(CassandraSessionFactory factory) {
        this.cassandraSessionFactory = factory;
    }
}
