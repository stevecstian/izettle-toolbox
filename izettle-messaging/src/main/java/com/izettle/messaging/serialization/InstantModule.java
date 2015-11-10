package com.izettle.messaging.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class InstantModule extends SimpleModule {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
        .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .withZone(ZoneId.of("UTC"));

    public InstantModule() {
        this.addSerializer(new InstantSerializer());
        this.addDeserializer(Instant.class, new InstantDeserializer());
    }

    public static class InstantSerializer extends JsonSerializer<Instant> {
        @Override
        public void serialize(
            Instant value,
            JsonGenerator jsonGenerator,
            SerializerProvider serializers
        ) throws IOException {
            jsonGenerator.writeString(DATE_TIME_FORMATTER.format(value));
        }

        @Override
        public Class<Instant> handledType() {
            return Instant.class;
        }
    }

    private static class InstantDeserializer extends JsonDeserializer<Instant> {
        @Override
        public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            try {
                return LocalDateTime.parse(p.getText(), DATE_TIME_FORMATTER).toInstant(ZoneOffset.UTC);
            } catch (DateTimeParseException e) {
                return Instant.parse(p.getText());
            }
        }
    }
}
