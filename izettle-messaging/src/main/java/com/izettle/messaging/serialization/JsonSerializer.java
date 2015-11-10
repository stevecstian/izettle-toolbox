package com.izettle.messaging.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonSerializer {
    private static final ObjectMapper JSON_MAPPER = createInstance();

    private static ObjectMapper createInstance() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        //Configure java.time.Instant serialization/deserialization.
        objectMapper.registerModule(new InstantModule());

        return objectMapper;
    }

    public static ObjectMapper getInstance() {
        return JSON_MAPPER;
    }
}
