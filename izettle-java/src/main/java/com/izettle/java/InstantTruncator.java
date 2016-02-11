package com.izettle.java;

import static com.izettle.java.CalendarTruncator.truncateInstant;

import com.izettle.java.CalendarTruncator.CalendarField;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class InstantTruncator {

    private InstantTruncator() {
    }

    /**
     * Truncates the given instant to the nearest previous <code>ChronoUnit</code>: all lesser units are set to zero.
     * This method acts as a complement to the standard <code>truncatedTo</code> method available on the instant. This
     * is to be able to truncate units larger than a "day".
     * @param instant The instant to truncate
     * @param timeZoneId The time zone to be taken into consideration when truncating (not really needed when truncating
     *        smaller fields, but then again: this method is not really necessary.
     * @param chronoUnit The chrono unit to truncate to
     * @return a newly created copy of the given instant, with the truncated value
     */
    public static Instant truncate(
        final Instant instant,
        final TimeZoneId timeZoneId,
        final ChronoUnit chronoUnit
    ) {
        if (chronoUnit.getDuration().compareTo(ChronoUnit.DAYS.getDuration()) <= 0) {
            //Up until DAYS are supported in the native functions:
            return ZonedDateTime
                .ofInstant(instant, timeZoneId.toZoneId())
                .truncatedTo(chronoUnit)
                .toInstant();
        } else {
            //Need to do some special handling, as these units are not supported by the native functions (but whyyy?)
            //We loose precision below millis here, but as we're truncating we don't need them anyway
            return truncateInstant(timeZoneId, getCalendarField(chronoUnit), Date.from(instant)).toInstant();
        }
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

    private static CalendarField getCalendarField(final ChronoUnit chronoUnit) {
        switch (chronoUnit) {
            case SECONDS:
                return CalendarField.SECOND;
            case MINUTES:
                return CalendarField.MINUTE;
            case HOURS:
                return CalendarField.HOUR;
            case DAYS:
                return CalendarField.DAY;
            case WEEKS:
                return CalendarField.WEEK;
            case MONTHS:
                return CalendarField.MONTH;
            case YEARS:
                return CalendarField.YEAR;
            case NANOS:
            case MICROS:
            case MILLIS:
            case HALF_DAYS:
            case DECADES:
            case CENTURIES:
            case MILLENNIA:
            case ERAS:
            case FOREVER:
            default:
                throw new IllegalArgumentException("Cannot truncate chronoUnit: " + chronoUnit);

        }
    }

}
