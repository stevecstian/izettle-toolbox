package com.izettle.java;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.Test;

public class InstantTruncatorSpec {

    private static final TimeZoneId TIME_ZONE_ID = TimeZoneId.UTC;

    @Test
    public void itShouldAddWeeks() throws Exception {
        final Instant instant = Instant.now();
        final Instant result = InstantTruncator.plus(instant, TIME_ZONE_ID, 1, ChronoUnit.WEEKS);
        final Instant expected = ZonedDateTime
            .ofInstant(instant, TIME_ZONE_ID.toZoneId())
            .plus(1, ChronoUnit.WEEKS)
            .toInstant();
        assertEquals(expected, result);
    }

    @Test
    public void itShouldAddYears() throws Exception {
        final Instant instant = Instant.now();
        final Instant result = InstantTruncator.plus(instant, TIME_ZONE_ID, 1, ChronoUnit.YEARS);
        final Instant expected = ZonedDateTime
            .ofInstant(instant, TIME_ZONE_ID.toZoneId())
            .plus(1, ChronoUnit.YEARS)
            .toInstant();
        assertEquals(expected, result);
    }
}
