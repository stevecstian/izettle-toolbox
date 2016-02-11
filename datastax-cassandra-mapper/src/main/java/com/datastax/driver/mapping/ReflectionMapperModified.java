package com.datastax.driver.mapping;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.DataType;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An {@link EntityMapper} implementation that use reflection to read and write fields
 * of an entity.
 */
class ReflectionMapperModified<T> extends EntityMapper<T> {

    private static ReflectionFactory factory = new ReflectionFactory();

    private ReflectionMapperModified(
        Class<T> entityClass,
        String keyspace,
        String table,
        ConsistencyLevel writeConsistency,
        ConsistencyLevel readConsistency
    ) {
        super(entityClass, keyspace, table, writeConsistency, readConsistency);
    }

    public static Factory factory() {
        return factory;
    }

    @Override
    public T newEntity() {
        throw new UnsupportedOperationException();
    }

    private static class LiteralMapper<T> extends ColumnMapper<T> {

        private final Method readMethod;

        private LiteralMapper(Field field, int position, PropertyDescriptor pd, AtomicInteger columnNumber) {
            this(field, extractSimpleType(field), position, pd, columnNumber);
        }

        private LiteralMapper(
            Field field,
            DataType type,
            int position,
            PropertyDescriptor pd,
            AtomicInteger columnCounter
        ) {
            super(field, type, position, columnCounter);
            this.readMethod = pd.getReadMethod();
        }

        @Override
        public Object getValue(T entity) {
            try {
                return readMethod.invoke(entity);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Could not get field '" + fieldName + "'");
            } catch (Exception e) {
                throw new IllegalStateException(
                    "Unable to access getter for '" + fieldName + "' in " + entity.getClass().getName(),
                    e
                );
            }
        }

        @Override
        public void setValue(Object entity, Object value) {
            throw new UnsupportedOperationException();
        }
    }

    private static class EnumMapper<T> extends LiteralMapper<T> {

        private final EnumType enumType;
        private final Map<String, Object> fromString;

        private EnumMapper(
            Field field,
            int position,
            PropertyDescriptor pd,
            EnumType enumType,
            AtomicInteger columnCounter
        ) {
            super(field, enumType == EnumType.STRING ? DataType.text() : DataType.cint(), position, pd, columnCounter);
            this.enumType = enumType;

            if (enumType == EnumType.STRING) {
                fromString = new HashMap<>(javaType.getEnumConstants().length);
                for (Object constant : javaType.getEnumConstants()) {
                    fromString.put(constant.toString().toLowerCase(), constant);
                }
            } else {
                fromString = null;
            }
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Object getValue(T entity) {
            Object value = super.getValue(entity);
            switch (enumType) {
                case STRING:
                    return (value == null) ? null : value.toString();
                case ORDINAL:
                    return (value == null) ? null : ((Enum) value).ordinal();
            }
            throw new AssertionError();
        }

        @Override
        public void setValue(Object entity, Object value) {
            throw new UnsupportedOperationException();
        }
    }

    static DataType extractSimpleType(Field f) {
        Type type = f.getGenericType();

        assert !(type instanceof ParameterizedType);

        if (!(type instanceof Class)) {
            throw new IllegalArgumentException(String.format("Cannot map class %s for field %s", type, f.getName()));
        }

        return TypeMappings.getSimpleType((Class<?>) type, f.getName());
    }

    private static class ReflectionFactory implements Factory {

        public <T> EntityMapper<T> create(
            Class<T> entityClass,
            String keyspace,
            String table,
            ConsistencyLevel writeConsistency,
            ConsistencyLevel readConsistency
        ) {
            return new ReflectionMapperModified<>(entityClass, keyspace, table, writeConsistency, readConsistency);
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        public <T> ColumnMapper<T> createColumnMapper(
            Class<T> entityClass,
            Field field,
            int position,
            MappingManager mappingManager,
            AtomicInteger columnCounter
        ) {
            String fieldName = field.getName();
            try {
                for (PropertyDescriptor pd : Introspector.getBeanInfo(field.getDeclaringClass()).getPropertyDescriptors()) {
                    if (pd.getReadMethod() != null && field.getName().equals(pd.getName())) {
                        if (field.getType().isEnum()) {
                            return new EnumMapper<>(field, position, pd, AnnotationParser.enumType(field), columnCounter);
                        }

                        if (field.getGenericType() instanceof ParameterizedType) {
                            InferredCQLType inferredCQLType = InferredCQLType.from(field, mappingManager);
                            return new LiteralMapper<>(field, inferredCQLType.dataType, position, pd, columnCounter);
                        }
                        return new LiteralMapper<>(field, position, pd, columnCounter);
                    }
                }

            } catch (IntrospectionException e) {
                // pass through
            }
            throw new IllegalArgumentException("Cannot find matching getter for field '" + fieldName + "'");
        }
    }
}
