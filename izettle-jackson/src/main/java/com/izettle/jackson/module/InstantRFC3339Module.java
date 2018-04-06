package com.izettle.jackson.module;

import static com.izettle.java.DateTimeFormatters.RFC_3339_INSTANT;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;

/**
 * A simple Jackson module that will serialize and deserialize an Instant in accordance with RFC3339
 * The default jackson module will use only the Zulu 'Z' zone indicator, which breaks compatibility with
 * quite a few clients. This module ensures that both variants can be parsed, but that +0000 is always used in the
 * serialized representation.
 *
 * Note: This module needs to be registered after other possible modules that might try to control `Instant`, such as
 * the JaveTimeModule.
 */
public class InstantRFC3339Module extends SimpleModule {

    private static final StdDateFormat STD_DATE_FORMAT = new StdDateFormat();
    /**
     * Note: This module needs to be registered after other possible modules that might try to control `Instant`, such
     * as the JaveTimeModule.
     */
    public InstantRFC3339Module() {
        super();
        addDeserializer(Instant.class, new InstantDeserializer());
        addSerializer(Instant.class, new InstantSerializer());
    }

    public static class InstantSerializer extends JsonSerializer<Instant> {

        @Override
        public void serialize(
            final Instant instant,
            final JsonGenerator jsonGenerator,
            final SerializerProvider serializerProvider
        ) throws IOException {
            jsonGenerator.writeString(RFC_3339_INSTANT.format(instant));
        }
    }

    public static class InstantDeserializer extends JsonDeserializer<Instant> {
        @Override
        public Instant deserialize(
            final JsonParser jp,
            final DeserializationContext context
        ) throws IOException {
            final String value = jp.readValueAs(String.class);
            try {
                return STD_DATE_FORMAT.parse(value).toInstant();
            } catch (ParseException e) {
                throw new IOException(e);
            }
        }
    }
}
