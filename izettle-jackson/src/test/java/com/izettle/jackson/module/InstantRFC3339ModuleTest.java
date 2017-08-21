package com.izettle.jackson.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.Test;

public class InstantRFC3339ModuleTest {

    private static ObjectMapper createMapper() {
        return new ObjectMapper()
            .registerModule(new InstantRFC3339Module());
    }

    @Test
    public void itShouldInterpretOffsetCorrectly() throws Exception {
        final ObjectMapper mapper = createMapper();
        final Instant parsedInstant = mapper.readValue("\"2016-08-04T09:42:51.336+0200\"", Instant.class);
        assertEquals(Instant.parse("2016-08-04T07:42:51.336Z"), parsedInstant);
    }

    @Test
    public void itShouldAlwaysProduceMillis() throws Exception {
        final ObjectMapper mapper = createMapper();
        final Instant nowTruncatedToSeconds = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        final String actual = mapper.writeValueAsString(nowTruncatedToSeconds);
        assertTrue(
            String.format(
                "Expected the output to end with millis and zero offset '000+0000', but when serializing %s the result "
                + "was: %s",
                nowTruncatedToSeconds,
                actual
            ),
            actual.contains(".000+0000")
        );
    }

    @Test
    public void itShouldProduceZeroOffset() throws Exception {
        final ObjectMapper mapper = createMapper();
        final Instant now = Instant.now();
        final String actual = mapper.writeValueAsString(now);
        assertTrue(
            String.format(
                "Expected the output to end with zero offset '+0000', but when serializing %s the result was: %s",
                now,
                actual
            ),
            actual.contains("+0000")
        );
    }

    @Test
    public void itShouldHandleZuluIndicator() throws Exception {
        final ObjectMapper mapper = createMapper();
        final String value = "\"2016-08-04T09:42:51.336Z\"";
        final Instant parsedInstant = mapper.readValue(value, Instant.class);
        assertEquals(Instant.parse("2016-08-04T09:42:51.336Z"), parsedInstant);
    }

    @Test
    public void itShouldHandleZeroMillis() throws Exception {
        final ObjectMapper mapper = createMapper();
        final String value = "\"2017-02-16T16:33:55Z\"";
        final Instant parsedInstant = mapper.readValue(value, Instant.class);
        assertEquals(Instant.parse("2017-02-16T16:33:55Z"), parsedInstant);
    }

    @Test
    public void itShouldHandleEpochTimestamp() throws IOException {
        final ObjectMapper mapper = createMapper();
        final String value = "1437495773948";
        final Instant parsedInstant = mapper.readValue(value, Instant.class);
        assertEquals(Instant.parse("2015-07-21T16:22:53.948Z"), parsedInstant);
    }
}
