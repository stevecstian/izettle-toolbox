package com.izettle.jackson.module;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * A simple Jackson module that will serialize and deserialize a BigDecimal as a string with the simple purpose of
 * keeping the exact precision during conversions. Other default modules and their configurations in Jackson will either
 * produce scientific notation, or simply create a json numeral that might loose precision along the way. For example
 * {"quantity" : "1.200"} might be converted to {"quantity" : 1.200} which will loose its trailing zeroes to 1.2
 *
 * This module simple ensures that BigDecimals in Java is always serialized/deserialized as a quoted string in json
 */
public class BigDecimalModule extends SimpleModule {

    public BigDecimalModule() {
        super();
        addDeserializer(BigDecimal.class, new BigDecimalDeserializer());
        addSerializer(BigDecimal.class, new BigDecimalSerializer());
    }

    public static class BigDecimalSerializer extends JsonSerializer<BigDecimal> {

        @Override
        public void serialize(
            final BigDecimal bigDecimal,
            final JsonGenerator jsonGenerator,
            final SerializerProvider serializerProvider
        ) throws IOException {
            jsonGenerator.writeObject(bigDecimal.toPlainString());
        }
    }

    private static class BigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

        @Override
        public BigDecimal deserialize(
            final JsonParser jp,
            final DeserializationContext context
        ) throws IOException, JsonProcessingException {
            final String value = jp.readValueAs(String.class);
            return new BigDecimal(value);
        }
    }
}

