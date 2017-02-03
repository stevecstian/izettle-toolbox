package com.izettle.messaging.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.izettle.jackson.module.InstantRFC3339Module;

public class JsonSerializer {
    private static final ObjectMapper JSON_MAPPER = createInstance();

    private static ObjectMapper createInstance() {
        ObjectMapper result = new ObjectMapper();
        result.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        result.registerModule(new JavaTimeModule());
        result.registerModule(new Jdk8Module());
        result.registerModule(new ParameterNamesModule());
        result.registerModule(new UUIDModule());
        result.registerModule(new InstantRFC3339Module());
        return result;
    }

    public static ObjectMapper getInstance() {
        return JSON_MAPPER;
    }
}
