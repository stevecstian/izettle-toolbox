package com.izettle.jackson.paramconverter;

import com.izettle.java.UUIDFactory;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.UUID;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;

/**
 * Provides a param converter that will accept UUIDs in resource paths as either HEX or BASE-64<p>
 *
 * To use in a dropwizard app, register it like you would any other Jersey component.<p>
 *
 * <code>environment.jersey().register(new UUIDParamConverterProvider());</code>
 */
public class UUIDParamConverterProvider implements ParamConverterProvider {
    @Override
    public <T> ParamConverter<T> getConverter(
        Class<T> rawType, Type genericType, Annotation[] annotations
    ) {
        if (rawType.getName().equals(UUID.class.getName())) {
            return new ParamConverter<T>() {
                @Override
                public T fromString(String value) {
                    return rawType.cast(UUIDFactory.parse(value));
                }

                @Override
                public String toString(T value) {
                    return value == null ? null : value.toString();
                }
            };
        }
        return null;
    }
}
