package com.izettle.java;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.WeekFields;

/**
 * Truncates an instant to the supplied ChronoUnit.<br>
 * <br>
 * For example creating an instance with {@link java.time.temporal.ChronoUnit#HOURS} and using it to adjust the
 * instant 2016-02-24T12:12:55.854Z will adjust the instant into 2016-02-24T12:00:00.000Z <br>
 * <br>
 * Adjusting an instant weeks will use the ISO-8601 standard where weeks start on Mondays
 */
public class InstantTruncateTemporalAdjuster implements TemporalAdjuster {

    private static ZoneId ZONE_ID = ZoneId.of("UTC");

    private ChronoUnit chronoUnit;

    public InstantTruncateTemporalAdjuster(ChronoUnit chronoUnit) {
        this.chronoUnit = chronoUnit;

        if (chronoUnit.compareTo(ChronoUnit.YEARS) > 0) {
            throw new UnsupportedTemporalTypeException("Only ChronoUnits equal or smaller than ChronoUnit.YEARS are supported");
        }
    }

    @Override
    public Temporal adjustInto(Temporal temporal) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.from(temporal), ZONE_ID);

        if (chronoUnit.compareTo(ChronoUnit.DAYS) <= 0) {
            localDateTime = localDateTime.truncatedTo(chronoUnit);
        } else {
            localDateTime = localDateTime.truncatedTo(ChronoUnit.DAYS);

            if (chronoUnit.compareTo(ChronoUnit.WEEKS) == 0) {
                localDateTime = localDateTime.with(WeekFields.ISO.dayOfWeek(), 1);
            }

            if (chronoUnit.compareTo(ChronoUnit.MONTHS) >= 0) {
                localDateTime = localDateTime.with(TemporalAdjusters.firstDayOfMonth());
            }

            if (chronoUnit.compareTo(ChronoUnit.YEARS) == 0) {
                localDateTime = localDateTime.with(TemporalAdjusters.firstDayOfYear());
            }
        }

        return temporal.with(localDateTime.toInstant(ZoneOffset.UTC));
    }

}
