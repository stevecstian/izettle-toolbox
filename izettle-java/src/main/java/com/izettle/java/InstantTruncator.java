package com.izettle.java;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class InstantTruncator {

    private InstantTruncator() {
    }

    /**
     * Utility method for easily adding time to an instant. This method acts as a complement to the standard
     * <code>Instant::plus</code> method which cannot take larger chrono units than DAY.
     * @param instant The instant to add time to
     * @param timeZoneId The time zone to be taken into consideration when adding
     * @param nr the number of units to add
     * @param chronoUnit The chrono unit to add
     * @return a newly created copy of the given instant, with the time added
     */
    public static Instant plus(
        final Instant instant,
        final TimeZoneId timeZoneId,
        final long nr,
        final ChronoUnit chronoUnit
    ) {
        return ZonedDateTime
            .ofInstant(instant, timeZoneId.toZoneId())
            .plus(nr, chronoUnit)
            .toInstant();
    }

    /**
     * Utility method for easily subtracting time from an instant. This method acts as a complement to the standard
     * <code>Instant::minus</code> method which cannot take larger chrono units than DAY.
     * @param instant The instant to subtract time from
     * @param timeZoneId The time zone to be taken into consideration when subtracting
     * @param nr the number of units to subtract
     * @param chronoUnit The chrono unit to subtract
     * @return a newly created copy of the given instant, with the time subtracted
     */
    public static Instant minus(
        final Instant instant,
        final TimeZoneId timeZoneId,
        final long nr,
        final ChronoUnit chronoUnit
    ) {
        return plus(instant, timeZoneId, -nr, chronoUnit);
    }

}
