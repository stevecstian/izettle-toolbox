package com.izettle.java;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import org.junit.Test;

public class InstantTruncateTemporalAdjusterSpec {

    private static final TimeZoneId TIME_ZONE_ID = TimeZoneId.UTC;

    @Test
    public void itShouldTruncateNanoseconds() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:10Z").with(ChronoField.NANO_OF_SECOND, 50);
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.NANOS));
        final Instant expected = Instant.parse("2016-02-01T20:50:10Z").with(ChronoField.NANO_OF_SECOND, 50);

        assertThat(actual.compareTo(expected)).isEqualTo(0);
    }

    @Test
    public void itShouldTruncateMilliseconds() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:10Z").with(ChronoField.NANO_OF_SECOND, 50);
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.MILLIS));
        final Instant expected = Instant.parse("2016-02-01T20:50:10Z");

        assertThat(actual.compareTo(expected)).isEqualTo(0);
    }

    @Test
    public void itShouldTruncateSeconds() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:10Z").with(ChronoField.NANO_OF_SECOND, 50);
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.SECONDS));
        final Instant expected = Instant.parse("2016-02-01T20:50:10Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateMinutes() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:10Z");
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.MINUTES));
        final Instant expected = Instant.parse("2016-02-01T20:50:00Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateHours() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:01Z");
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.HOURS));
        final Instant expected = Instant.parse("2016-02-01T20:00:00Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateDays() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:01Z");
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.DAYS));
        final Instant expected = Instant.parse("2016-02-01T00:00:00Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateWeeks() throws Exception {
        final Instant instant = Instant.parse("2016-02-10T20:50:01Z");
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.WEEKS));
        final Instant expected = Instant.parse("2016-02-08T00:00:00Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateYears() throws Exception {
        final Instant instant = Instant.parse("2016-02-03T20:50:01Z");
        final Instant actual = instant.with(new InstantTruncateTemporalAdjuster(ChronoUnit.YEARS));
        final Instant expected = Instant.parse("2016-01-01T00:00:00Z");

        assertThat(actual).isEqualTo(expected);
    }

}
