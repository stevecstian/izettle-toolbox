package com.izettle.messaging.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.izettle.java.UUIDFactory;
import java.io.IOException;
import java.util.UUID;

public class UUIDModule extends SimpleModule {

    public UUIDModule() {
        super();
        addDeserializer(UUID.class, new UUIDDeserializer());
        addSerializer(UUID.class, new UUIDSerializer());
    }

    public static class UUIDSerializer extends JsonSerializer<UUID> {

        @Override
        public void serialize(
            final UUID uuid,
            final JsonGenerator jsonGenerator,
            final SerializerProvider serializerProvider
        ) throws IOException {
            jsonGenerator.writeString(uuid.toString());
        }
    }

    public static class UUIDDeserializer extends JsonDeserializer<UUID> {

        @Override
        public UUID deserialize(
            final JsonParser jp,
            final DeserializationContext context
        ) throws IOException {
            final String value = jp.readValueAs(String.class);
            return UUIDFactory.parse(value);
        }
    }
}
