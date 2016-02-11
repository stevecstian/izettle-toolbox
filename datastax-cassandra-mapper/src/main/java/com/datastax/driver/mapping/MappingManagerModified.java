package com.datastax.driver.mapping;

import com.datastax.driver.core.Session;
import java.util.HashMap;
import java.util.Map;

public class MappingManagerModified extends MappingManager {

    private final Map<String, EntityMapper<?>> entityMapperCache = new HashMap<>();

    public MappingManagerModified(Session session) {
        super(session);
    }

    @Override
    public <T> Mapper<T> mapper(Class<T> klass) {
        EntityMapper<T> entityMapper = AnnotationParser.parseEntity(klass, ReflectionMapperModified.factory(), this);
        return new Mapper<>(this, klass, entityMapper);
    }

    @SuppressWarnings("unchecked")
    public <T> EntityMapper<T> entityMapper(Class<T> klass) {
        return (EntityMapper<T>) entityMapperCache.computeIfAbsent(
            klass.getName(),
            className -> AnnotationParser.parseEntity(klass, ReflectionMapperModified.factory(), this)
        );
    }
}
