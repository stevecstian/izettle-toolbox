package com.izettle.java;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class CalendarTruncator {

    private CalendarTruncator() {
    }

    /**
     * Figures out the first instant for the year, given the provided time zone
     * @param timeZoneId The time zone of the spectator
     * @param year the year
     * @return The Date of the first instant of the provided year
     */
    public static Date getFirstInstantOfYear(TimeZoneId timeZoneId, int year) {
        Calendar resultDate = CalendarCreator.create(timeZoneId);
        resultDate.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        resultDate.set(Calendar.MILLISECOND, 0);
        resultDate.set(Calendar.YEAR, year);
        return resultDate.getTime();
    }

    /**
     * Figures out the first instant for the month, given the provided time zone
     * @param timeZoneId The time zone of the spectator
     * @param year the year
     * @param month the month, 1-12. Values exceeding 12 will increment the year correspondingly
     * @return The Date of the first instant of the provided month
     */
    public static Date getFirstInstantOfMonth(TimeZoneId timeZoneId, int year, int month) {
        int y = year + (month - 1) / 12;
        int m = (month - 1) % 12;
        Calendar resultDate = CalendarCreator.create(timeZoneId);
        resultDate.set(y, m, 1, 0, 0, 0);
        resultDate.set(Calendar.MILLISECOND, 0);
        return resultDate.getTime();
    }

    /**
     * Figures out the first instant for the week, given the provided time zone
     * @param timeZoneId The time zone of the spectator
     * @param year the year
     * @param weekNumber the number of the week 1-52/53 (depending on the actual year)
     * @return The Date of the first instant of the provided week
     */
    public static Date getFirstInstantOfWeek(TimeZoneId timeZoneId, int year, int weekNumber) {
        Calendar resultDate = CalendarCreator.create(timeZoneId);
        resultDate.setFirstDayOfWeek(Calendar.MONDAY);
        resultDate.setMinimalDaysInFirstWeek(4);
        resultDate.set(year, Calendar.JANUARY, 1, 0, 0, 0);
        resultDate.set(Calendar.MILLISECOND, 0);
        int maxWeekNr = resultDate.getActualMaximum(Calendar.WEEK_OF_YEAR);
        if (weekNumber > maxWeekNr) {
            throw new IllegalArgumentException("Week number " + weekNumber + " of year " + year + " is invalid.");
        }
        resultDate.set(Calendar.WEEK_OF_YEAR, weekNumber);
        resultDate.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return resultDate.getTime();
    }

    /**
     * Figures out the first instant for the date String, given the provided time zone
     *
     * @param timeZoneId The time zone of the spectator
     * @param dateString the date String (yyyy-MM-dd)
     * @return The Date of the first instant of the provided date String
     * @throws ParseException when the date String is in the wrong format (yyyy-MM-dd)
     */
    public static Date getFirstInstantOfDay(TimeZoneId timeZoneId, String dateString) throws ParseException {
        DateFormat formatter = DateFormatCreator.createDateFormatter(timeZoneId);
        return formatter.parse(dateString);
    }

    public enum CalendarField {

        SECOND,
        MINUTE,
        HOUR,
        DAY,
        /**
         * ISO 8601 Weeks starting on Mondays, with week 01 being the week with the year's first Thursday in it
         */
        WEEK,
        MONTH,
        YEAR
    }

    /**
     * Forwards the instant the specified amount of the provided field. This method takes the time zone into
     * consideration. Note that other fields than the specified will often be affected by this operation, as some
     * forwards will spill over to larger fields, alternatively edge cases affecting lesser fields.
     *
     * Example:
     * given the instant '2001-01-01 05:30:12.345', forwardInstant 1 HOUR will give '2001-01-01 06:30:12.345'
     * given the instant '2013-01-29 14:15:16.789', forwardInstant 1 MONTH will give '2013-02-28 14:15:16.789'
     *
     *
     * @param timeZoneId The time zone of the spectator
     * @param field the type of field to forward
     * @param instant the instant
     * @param amount the amount to forward the calendar field, may be negative for going back in time
     * @return The first instant of the day of the provided instant
     */
    public static Date forwardInstant(TimeZoneId timeZoneId, CalendarField field, Date instant, int amount) {
        Calendar calendar = CalendarCreator.create(timeZoneId);
        calendar.setTime(instant);
        switch (field) {
            case YEAR:
                calendar.add(Calendar.YEAR, amount);
                break;
            case MONTH:
                calendar.add(Calendar.MONTH, amount);
                break;
            case WEEK:
                calendar.setMinimalDaysInFirstWeek(4);
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                calendar.add(Calendar.WEEK_OF_YEAR, amount);
                break;
            case DAY:
                calendar.add(Calendar.DAY_OF_YEAR, amount);
                break;
            case HOUR:
                calendar.add(Calendar.HOUR_OF_DAY, amount);
                break;
            case MINUTE:
                calendar.add(Calendar.MINUTE, amount);
                break;
            case SECOND:
                calendar.add(Calendar.SECOND, amount);
                break;
        }
        return calendar.getTime();
    }

    /**
     * Truncates the instant from all fields lesser than the provided field. This method takes the time zone into
     * consideration. Intended to behave in the same way as date_trunc('field', timestamp) in PGSQL:
     *
     * Example:
     * given the instant '2001-01-01 05:30:12.345', truncateInstant HOUR will give '2001-01-01 05:00:00.000'
     *
     *
     * Returns a new Date with all fields that are less significant than the selected one set to zero (or one, for
     * day of month).
     *
     * @param timeZoneId The time zone of the spectator
     * @param field The smallest field to keep untouched
     * @param instant the instant
     * @return The first instant of the day of the provided instant
     */
    public static Date truncateInstant(TimeZoneId timeZoneId, CalendarField field, Date instant) {
        Calendar calendar = CalendarCreator.create(timeZoneId);
        calendar.setTime(instant);
        switch (field) {
            case YEAR:
                calendar.set(Calendar.MONTH, 0);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                break;
            case MONTH:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                break;
            case WEEK:
                calendar.setMinimalDaysInFirstWeek(4);
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                break;
            case DAY:
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                break;
            case HOUR:
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                break;
            case MINUTE:
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                break;
            case SECOND:
                calendar.set(Calendar.MILLISECOND, 0);
                break;
        }
        return calendar.getTime();
    }

}
