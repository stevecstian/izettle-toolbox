package com.izettle.java;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.junit.Test;

public class InstantTruncatorSpec {

    final TimeZoneId TIME_ZONE_ID = TimeZoneId.UTC;

    @Test
    public void itShouldTruncateHours() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:01Z");
        final Instant truncated = InstantTruncator.truncate(instant, TIME_ZONE_ID, ChronoUnit.HOURS);
        final Instant expected = Instant.parse("2016-02-01T20:00:00Z");
        assertEquals(expected, truncated);
    }

    @Test
    public void itShouldTruncateWeeks() throws Exception {
        final Instant instant = Instant.parse("2016-02-03T20:50:01Z");
        final Instant truncated = InstantTruncator.truncate(instant, TIME_ZONE_ID, ChronoUnit.WEEKS);
        final Instant expected = Instant.parse("2016-02-01T00:00:00Z");
        assertEquals(expected, truncated);
    }

    @Test
    public void itShouldTruncateYears() throws Exception {
        final Instant instant = Instant.parse("2016-02-03T20:50:01Z");
        final Instant truncated = InstantTruncator.truncate(instant, TIME_ZONE_ID, ChronoUnit.YEARS);
        final Instant expected = Instant.parse("2016-01-01T00:00:00Z");
        assertEquals(expected, truncated);
    }

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
