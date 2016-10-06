package com.izettle.cassandra.migration;

import com.datastax.driver.core.TypeCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MigrationConfig<S, D> {

    private String name;
    private String sourceTableName;
    private String destinationTableName;
    private Integer fetchSize = 10000;
    private Class<S> sourceEntity;
    private Class<D> destinationEntity;
    private List<TypeCodec<?>> codecs = new ArrayList<>();
    private Function<S, D> sourceToDestinationMapper;


    public String getName() {
        return name;
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public String getDestinationTableName() {
        return destinationTableName;
    }

    public Class<S> getSourceEntity() {
        return sourceEntity;
    }

    public Class<D> getDestinationEntity() {
        return destinationEntity;
    }

    public List<TypeCodec<?>> getCodecs() {
        return codecs;
    }

    public Integer getFetchSize() {
        return fetchSize;
    }

    public Function<S, D> getSourceToDestinationMapper() {
        return sourceToDestinationMapper;
    }
}
