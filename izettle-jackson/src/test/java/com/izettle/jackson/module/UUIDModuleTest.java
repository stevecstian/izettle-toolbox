package com.izettle.jackson.module;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.izettle.java.UUIDFactory;
import java.util.UUID;
import org.junit.Test;

public class UUIDModuleTest {

    private static ObjectMapper createMapper() {
        return new ObjectMapper()
            .registerModule(new UUIDModule());
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldNotAcceptNonsense() throws Exception {
        final ObjectMapper mapper = createMapper();
        mapper.readValue("\"dummy data\"", UUID.class);
    }

    @Test
    public void itShouldAcceptNativeStyleLowerCase() throws Exception {
        final UUID uuid = UUIDFactory.createUUID1();
        final ObjectMapper mapper = createMapper();
        final UUID parsedUuid = mapper.readValue("\"" + uuid.toString().toLowerCase() + "\"", UUID.class);
        assertEquals(uuid, parsedUuid);
    }

    @Test
    public void itShouldAcceptNativeStyleUpperCase() throws Exception {
        final UUID uuid = UUIDFactory.createUUID1();
        final ObjectMapper mapper = createMapper();
        final UUID parsedUuid = mapper.readValue("\"" + uuid.toString().toUpperCase() + "\"", UUID.class);
        assertEquals(uuid, parsedUuid);
    }

    @Test
    public void itShouldAcceptBase64Style() throws Exception {
        final UUID uuid = UUIDFactory.createUUID1();
        final ObjectMapper mapper = createMapper();
        final UUID parsedUuid = mapper.readValue("\"" + UUIDFactory.toBase64String(uuid) + "\"", UUID.class);
        assertEquals(uuid, parsedUuid);
    }

    @Test
    public void itShouldSerializeToNativeFormat() throws Exception {
        final UUID uuid = UUIDFactory.createUUID1();
        final ObjectMapper mapper = createMapper();
        final String serializedUuid = mapper.writeValueAsString(uuid);
        assertEquals("\"" + uuid + "\"", serializedUuid);
    }
}
