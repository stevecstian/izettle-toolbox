package com.izettle.java;

import static com.izettle.java.InstantAdjusters.truncationBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.catchThrowable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import org.junit.Test;

public class InstantAdjustersTest {

    private static ZoneId zoneId = ZoneId.of("UTC");

    @Test
    public void itShouldTruncateNanoseconds() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:10Z").with(ChronoField.NANO_OF_SECOND, 50);
        final Instant actual = instant.with(truncationBy(ChronoUnit.NANOS, zoneId));
        final Instant expected = Instant.parse("2016-02-01T20:50:10Z").with(ChronoField.NANO_OF_SECOND, 50);

        assertThat(actual.compareTo(expected)).isEqualTo(0);
    }

    @Test
    public void itShouldTruncateMilliseconds() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:10Z").with(ChronoField.NANO_OF_SECOND, 50);
        final Instant actual = instant.with(truncationBy(ChronoUnit.MILLIS, zoneId));
        final Instant expected = Instant.parse("2016-02-01T20:50:10Z");

        assertThat(actual.compareTo(expected)).isEqualTo(0);
    }

    @Test
    public void itShouldTruncateSeconds() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:10Z").with(ChronoField.NANO_OF_SECOND, 50);
        final Instant actual = instant.with(truncationBy(ChronoUnit.SECONDS, zoneId));
        final Instant expected = Instant.parse("2016-02-01T20:50:10Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateMinutes() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:10Z");
        final Instant actual = instant.with(truncationBy(ChronoUnit.MINUTES, zoneId));
        final Instant expected = Instant.parse("2016-02-01T20:50:00Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateHours() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:01Z");
        final Instant actual = instant.with(truncationBy(ChronoUnit.HOURS, zoneId));
        final Instant expected = Instant.parse("2016-02-01T20:00:00Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateDays() throws Exception {
        final Instant instant = Instant.parse("2016-02-01T20:50:01Z");
        final Instant actual = instant.with(truncationBy(ChronoUnit.DAYS, zoneId));
        final Instant expected = Instant.parse("2016-02-01T00:00:00Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateWeeks() throws Exception {
        final Instant instant = Instant.parse("2016-02-10T20:50:01Z");
        final Instant actual = instant.with(truncationBy(ChronoUnit.WEEKS, zoneId));
        final Instant expected = Instant.parse("2016-02-08T00:00:00Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldTruncateYears() throws Exception {
        final Instant instant = Instant.parse("2016-02-03T20:50:01Z");
        final Instant actual = instant.with(truncationBy(ChronoUnit.YEARS, zoneId));
        final Instant expected = Instant.parse("2016-01-01T00:00:00Z");

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void itShouldThrowExceptionWhenChronoUnitIsNotSupported() {
        final Throwable thrown = catchThrowable(() -> truncationBy(ChronoUnit.MILLENNIA, zoneId));

        assertThat(thrown).isInstanceOf(UnsupportedTemporalTypeException.class);
    }

}
