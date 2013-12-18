package com.izettle.java;

import com.izettle.java.enums.TimeZoneId;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CalendarUtils {

	private CalendarUtils() {
	}

	public static Calendar createCalendar(TimeZoneId timeZoneId) {
		return Calendar.getInstance(timeZoneId.getTimeZone());
	}

	public static Calendar createCalendar(TimeZoneId timeZoneId, Locale locale) {
		return Calendar.getInstance(timeZoneId.getTimeZone(), locale);
	}

	/**
	 *
	 * This method will add the appropriate date count, to delay something a certain workday count.
	 * When passing in 0 as nrWorkDays, the same day will be returned in case of a workday. For Saturday and Sunday
	 * the following Monday is returned.
	 * Note that bank holidays are not taken into consideration.
	 *
	 * @param timeZoneId The time zone to use when doing the calendar calculation
	 * @param fromDate - the date to start the workday delay from.
	 * @param nrWorkDays - number of work days to delay.
	 * @return the date after delayWorkdays have been applied.
	 */
	public static Date addWorkDays(TimeZoneId timeZoneId, Date fromDate, final int nrWorkDays) {
		Calendar calendar = createCalendar(timeZoneId);
		calendar.setTime(fromDate);
		int delayDaysRemaining = nrWorkDays;
		boolean isWorkday;
		while (true) {
			isWorkday = Calendar.SATURDAY != calendar.get(Calendar.DAY_OF_WEEK) && Calendar.SUNDAY != calendar.get(Calendar.DAY_OF_WEEK);
			if (isWorkday) {
				if (delayDaysRemaining <= 0) {
					break;
				}
				delayDaysRemaining--;
			}
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		return calendar.getTime();
	}

	public static Date addWorkdaysFromNow(TimeZoneId timeZoneId, final int nrWorkDays) {
		return addWorkDays(timeZoneId, new Date(), nrWorkDays);
	}

	/**
	 * Will return the number of calendar days between the two instants
	 * @param fromInstant the earlier date
	 * @param untilInstant the later date
	 * @param timeZoneId The timezone to use when doing the calendar calculation
	 * @return until - from, expressed in nr of days
	 */
	public static int dateDiff(TimeZoneId timeZoneId, Date fromInstant, Date untilInstant) {
		Calendar fromCal = createCalendar(timeZoneId);
		fromCal.setTime(fromInstant);
		fromCal.set(Calendar.MILLISECOND, 0);
		fromCal.set(Calendar.SECOND, 0);
		fromCal.set(Calendar.MINUTE, 0);
		fromCal.set(Calendar.HOUR_OF_DAY, 0);
		Calendar toCal = createCalendar(timeZoneId);
		toCal.setTime(untilInstant);
		toCal.set(Calendar.MILLISECOND, 0);
		toCal.set(Calendar.SECOND, 0);
		toCal.set(Calendar.MINUTE, 0);
		toCal.set(Calendar.HOUR_OF_DAY, 0);
		long millis = toCal.getTimeInMillis() - fromCal.getTimeInMillis();
		return (int) TimeUnit.DAYS.convert(millis, TimeUnit.MILLISECONDS);
	}

	/**
	 * Figures out the first instant for the year, given the provided time zone
	 * @param timeZoneId The time zone of the spectator
	 * @param year the year
	 * @return The Date of the first instant of the provided year
	 */
	public static Date getFirstInstantOfYear(TimeZoneId timeZoneId, int year) {
		Calendar resultDate = createCalendar(timeZoneId);
		resultDate.set(year, 0, 1, 0, 0, 0);
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
		Calendar resultDate = createCalendar(timeZoneId);
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
		Calendar resultDate = createCalendar(timeZoneId);
		resultDate.setFirstDayOfWeek(Calendar.MONDAY);
		resultDate.setMinimalDaysInFirstWeek(4);
		resultDate.set(year, 0, 1, 0, 0, 0);
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
		DateFormat formatter = TimeUtils.createDateFormatter(timeZoneId);
		return formatter.parse(dateString);
	}

	public static enum CalendarField {

		SECOND,
		MINUTE,
		HOUR,
		DAY,
		MONTH,
		YEAR
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
		/*
		 * No earlier than 30 minues past midnight, local time, We want to clear all last days
		 * transactions for each country
		 */
		Calendar calendar = createCalendar(timeZoneId);
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
