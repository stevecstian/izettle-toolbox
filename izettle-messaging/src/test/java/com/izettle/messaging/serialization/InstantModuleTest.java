package com.izettle.messaging.serialization;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import org.junit.Test;

public class InstantModuleTest {

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new InstantModule());

    @Test
    public void testInstantSerializationDeserialization() throws Exception {
        String instantString = objectMapper.writeValueAsString(Instant.EPOCH);
        assertEquals("\"1970-01-01T00:00:00.000+0000\"", instantString);
        Instant actualEpoch = objectMapper.readValue(instantString, Instant.class);
        assertEquals(Instant.EPOCH, actualEpoch);
    }

    @Test
    public void testAdamsBirthdaySerialization() throws Exception {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.of("UTC"));
        Instant ctoEpoch = LocalDateTime.parse("1974-05-25T13:33:33.337+0000", format).toInstant(ZoneOffset.UTC);
        String instantString = objectMapper.writeValueAsString(ctoEpoch);
        assertEquals("\"1974-05-25T13:33:33.337+0000\"", instantString);
        Instant deserialized = objectMapper.readValue(instantString, Instant.class);
        assertEquals(ctoEpoch, deserialized);

    }
}
