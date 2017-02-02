package com.izettle.jackson.module;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.Test;

public class BigDecimalModuleTest {

    private static ObjectMapper createMapper() {
        return new ObjectMapper().registerModule(new BigDecimalModule());
    }

    @Test
    public void itShouldDeserializeAsExpected() throws Exception {
        assertEquals(new BigDecimal("1.00"), deserialize("1.00"));
        assertEquals(new BigDecimal("1.0"), deserialize("1.0"));
        assertEquals(new BigDecimal("1"), deserialize("1"));
        assertEquals(new BigDecimal("1.00"), deserialize("01.00"));
        assertEquals(new BigDecimal("0.00000010"), deserialize("0.00000010"));
    }

    @Test
    public void itShouldSerializeAsExpected() throws Exception {
        assertEquals("1.00", serialize(new BigDecimal("1.00")));
        assertEquals("1.0", serialize(new BigDecimal("1.0")));
        assertEquals("1", serialize(new BigDecimal("1")));
        assertEquals("1.00", serialize(new BigDecimal("01.00")));
        assertEquals("0.00000010", serialize(new BigDecimal("0.00000010")));
    }

    private BigDecimal deserialize(String value) throws IOException {
        final ObjectMapper mapper = createMapper();
        return mapper.readValue("\"" + value + "\"", BigDecimal.class);
    }
    private String serialize(BigDecimal value) throws IOException {
        final ObjectMapper mapper = createMapper();
        return mapper.writeValueAsString(value).replace("\"", "");
    }
}
