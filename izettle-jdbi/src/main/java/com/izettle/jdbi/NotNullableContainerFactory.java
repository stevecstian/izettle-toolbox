package com.izettle.jdbi;

import com.izettle.jdbi.exception.NoResultException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import org.skife.jdbi.v2.ContainerBuilder;
import org.skife.jdbi.v2.tweak.ContainerFactory;

public class NotNullableContainerFactory implements ContainerFactory<Object> {

    @Override
    public boolean accepts(Class<?> type) {
        return !Optional.class.isAssignableFrom(type)
            && !Collection.class.isAssignableFrom(type)
            && !Iterator.class.isAssignableFrom(type);
    }

    @Override
    public ContainerBuilder<Object> newContainerBuilderFor(Class<?> type) {
        return new NotNullableContainerBuilder();
    }

    private static class NotNullableContainerBuilder implements ContainerBuilder<Object> {

        private Object obj;

        @Override
        public ContainerBuilder<Object> add(Object obj) {
            this.obj = obj;
            return this;
        }

        @Override
        public Object build() {
            if (obj == null) {
                throw new NoResultException("Returned object from DAO cannot be null");
            }
            return obj;
        }
    }
}
