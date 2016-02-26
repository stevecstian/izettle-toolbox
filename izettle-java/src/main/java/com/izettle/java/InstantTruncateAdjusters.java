package com.izettle.java;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

/**
 * Truncates an instant to the supplied ChronoUnit.<br>
 * <br>
 * For example creating an instance with {@link java.time.temporal.ChronoUnit#HOURS} and using it to adjust the
 * instant 2016-02-24T12:12:55.854Z will adjust the instant into 2016-02-24T12:00:00.000Z <br>
 * <br>
 * Adjusting an instant weeks will use the ISO-8601 standard where weeks start on Mondays
 */
public class InstantTruncateAdjusters {

    public static TemporalAdjuster truncationBy(ChronoUnit chronoUnit, ZoneId zoneId) {
        return temporal -> {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.from(temporal), zoneId);

            if (chronoUnit.compareTo(ChronoUnit.DAYS) <= 0) {
                zonedDateTime = zonedDateTime.truncatedTo(chronoUnit);
            } else {
                zonedDateTime = zonedDateTime.truncatedTo(ChronoUnit.DAYS);

                if (chronoUnit.compareTo(ChronoUnit.WEEKS) == 0) {
                    zonedDateTime = zonedDateTime.with(WeekFields.ISO.dayOfWeek(), 1);
                }

                if (chronoUnit.compareTo(ChronoUnit.MONTHS) >= 0) {
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.firstDayOfMonth());
                }

                if (chronoUnit.compareTo(ChronoUnit.YEARS) == 0) {
                    zonedDateTime = zonedDateTime.with(TemporalAdjusters.firstDayOfYear());
                }
            }

            return temporal.with(zonedDateTime.toInstant());
        };
    }
}

