package com.izettle.java;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import org.junit.Test;

public class InstantTruncateTemporalAdjusterSpec {

    private static ZoneId zoneId = ZoneId.of("UTC");

    @Test
    public void itShouldTruncateNanoseconds() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:10Z").with(ChronoField.NANO_OF_SECOND, 50);
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.NANOS, zoneId));
        final Instant expected = Instant.parse("2016-02-01T20:50:10Z").with(ChronoField.NANO_OF_SECOND, 50);

        assertThat(actual.compareTo(expected)).isEqualTo(0);
    }

    @Test
    public void itShouldTruncateMilliseconds() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:10Z").with(ChronoField.NANO_OF_SECOND, 50);
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.MILLIS, zoneId));
        final Instant expected = Instant.parse("2016-02-01T20:50:10Z");

        assertThat(actual.compareTo(expected)).isEqualTo(0);
    }

    @Test
    public void itShouldTruncateSeconds() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:10Z").with(ChronoField.NANO_OF_SECOND, 50);
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.SECONDS, zoneId));
        final Instant expected = Instant.parse("2016-02-01T20:50:10Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateMinutes() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:10Z");
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.MINUTES, zoneId));
        final Instant expected = Instant.parse("2016-02-01T20:50:00Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateHours() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:01Z");
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.HOURS, zoneId));
        final Instant expected = Instant.parse("2016-02-01T20:00:00Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateDays() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:01Z");
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.DAYS, zoneId));
        final Instant expected = Instant.parse("2016-02-01T00:00:00Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateWeeks() throws Exception {
        final Instant instant = Instant.parse("2016-02-10T20:50:01Z");
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.WEEKS, zoneId));
        final Instant expected = Instant.parse("2016-02-08T00:00:00Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateYears() throws Exception {
        final Instant instant = Instant.parse("2016-02-03T20:50:01Z");
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.YEARS, zoneId));
        final Instant expected = Instant.parse("2016-01-01T00:00:00Z");

        assertThat(actual).isEqualTo(expected);
    }

}
