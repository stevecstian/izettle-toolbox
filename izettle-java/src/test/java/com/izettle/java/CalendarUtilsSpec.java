package com.izettle.java;

import static com.izettle.java.CalendarUtils.CalendarField.DAY;
import static com.izettle.java.CalendarUtils.CalendarField.HOUR;
import static com.izettle.java.CalendarUtils.CalendarField.MINUTE;
import static com.izettle.java.CalendarUtils.CalendarField.MONTH;
import static com.izettle.java.CalendarUtils.CalendarField.SECOND;
import static com.izettle.java.CalendarUtils.CalendarField.YEAR;
import static com.izettle.java.CalendarUtils.addWorkDays;
import static com.izettle.java.CalendarUtils.dateDiff;
import static com.izettle.java.CalendarUtils.getFirstInstantOfDay;
import static com.izettle.java.CalendarUtils.getFirstInstantOfMonth;
import static com.izettle.java.CalendarUtils.getFirstInstantOfWeek;
import static com.izettle.java.CalendarUtils.getFirstInstantOfYear;
import static com.izettle.java.CalendarUtils.truncateInstant;
import static com.izettle.java.enums.TimeZoneId.EUROPE_HELSINKI;
import static com.izettle.java.enums.TimeZoneId.EUROPE_LONDON;
import static com.izettle.java.enums.TimeZoneId.EUROPE_STOCKHOLM;
import static com.izettle.java.enums.TimeZoneId.UTC;
import static java.util.Calendar.*;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import org.junit.Test;

public class CalendarUtilsSpec {

	//Tests
	@Test
	public void itShouldScheduleAtMonday() {
		int[] startAtWeekDays = new int[]{FRIDAY, SATURDAY, SUNDAY};
		int applyWorkdaysDelay;
		int[] expectedDelays;
		//
		//Running tests postponing 0 days.
		applyWorkdaysDelay = 0;
		expectedDelays = new int[]{0, 2, 1};
		//
		verifyDelays(expectedDelays, startAtWeekDays, applyWorkdaysDelay);
	}

	@Test
	public void itShouldNotScheduleAtSaturday() {
		Calendar cal = CalendarUtils.createCalendar(EUROPE_STOCKHOLM);
		cal.set(DAY_OF_WEEK, SATURDAY);
		Date scheduleDate = cal.getTime();
		Date delayedDate = addWorkDays(EUROPE_STOCKHOLM, scheduleDate, 0);
		//
		assertThat(scheduleDate).isNotEqualTo(delayedDate);
	}

	@Test
	public void itShouldDelayCorrectly() {
		int[] startAtWeekDays = new int[]{MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY};
		int applyWorkdaysDelay;
		int[] expectedDelays;
		//
		//Running tests postponing just one day, making sure that weekends are taken into consideration correctly
		applyWorkdaysDelay = 1;
		expectedDelays = new int[]{1, 1, 1, 1, 3, 3, 2};
		verifyDelays(expectedDelays, startAtWeekDays, applyWorkdaysDelay);
		//
		//Running tests postponing full 5 working days, making sure that all dates get 7 calendar days delay, except weekend days that will get more
		applyWorkdaysDelay = 5;
		expectedDelays = new int[]{7, 7, 7, 7, 7, 9, 8};
		verifyDelays(expectedDelays, startAtWeekDays, applyWorkdaysDelay);
		//
		//Postponing 7 working days, makind sure that the double weekend is taken into account
		applyWorkdaysDelay = 7;
		expectedDelays = new int[]{9, 9, 9, 11, 11, 11, 10};
		verifyDelays(expectedDelays, startAtWeekDays, applyWorkdaysDelay);
	}

	@Test
	public void itShouldDiffDates() throws ParseException {

		DateFormat df = TimeUtils.createDateAndTimeFormatter(EUROPE_STOCKHOLM);
		Object[][] fromUntilPairs = new Object[][]{
			//from                  //until               //expected
			{"2013-03-27 23:59:59", "2013-03-28 00:00:01", 1},
			{"2013-03-27 00:00:01", "2013-03-28 23:59:59", 1},
			{"2013-03-27 00:00:01", "2013-03-29 23:59:59", 2}
		};
		for (Object[] fromUntilPair : fromUntilPairs) {
			Date from = df.parse((String) fromUntilPair[0]);
			Date until = df.parse((String) fromUntilPair[1]);
			assertEquals(fromUntilPair[2], dateDiff(EUROPE_STOCKHOLM, from, until));
		}
	}

	@Test
	public void itShouldReturnCorrectFirstYearlyInstant() {
		DateFormat df = TimeUtils.createDateAndTimeMillisFormatter(EUROPE_STOCKHOLM);
		assertEquals("2013-01-01 00:00:00.000", df.format(getFirstInstantOfYear(EUROPE_STOCKHOLM, 2013)));
		assertEquals("2013-01-01 01:00:00.000", df.format(getFirstInstantOfYear(EUROPE_LONDON, 2013)));
		assertEquals("2012-12-31 23:00:00.000", df.format(getFirstInstantOfYear(EUROPE_HELSINKI, 2013)));
	}

	@Test
	public void itShouldReturnCorrectFirstMonthlyInstant() {
		DateFormat df = TimeUtils.createDateAndTimeMillisFormatter(EUROPE_STOCKHOLM);
		assertEquals("2013-02-01 00:00:00.000", df.format(getFirstInstantOfMonth(EUROPE_STOCKHOLM, 2013, 2)));
		assertEquals("2013-02-01 01:00:00.000", df.format(getFirstInstantOfMonth(EUROPE_LONDON, 2013, 2)));
		assertEquals("2013-01-31 23:00:00.000", df.format(getFirstInstantOfMonth(EUROPE_HELSINKI, 2013, 2)));
		//Testing roll over
		assertEquals("2014-01-01 00:00:00.000", df.format(getFirstInstantOfMonth(EUROPE_STOCKHOLM, 2013, 13)));
	}

	@Test
	public void itShouldReturnCorrectFirstWeeklyInstant() {
		DateFormat df = TimeUtils.createDateAndTimeMillisFormatter(EUROPE_STOCKHOLM);
		assertEquals("2013-04-08 00:00:00.000", df.format(getFirstInstantOfWeek(EUROPE_STOCKHOLM, 2013, 15)));
		assertEquals("2013-04-08 01:00:00.000", df.format(getFirstInstantOfWeek(EUROPE_LONDON, 2013, 15)));
		assertEquals("2013-04-07 23:00:00.000", df.format(getFirstInstantOfWeek(EUROPE_HELSINKI, 2013, 15)));
	}

	@Test
	public void itShouldReturnCorrectFirstInstantOfDateString() throws ParseException {
		DateFormat df = TimeUtils.createDateAndTimeMillisFormatter(EUROPE_STOCKHOLM);
		assertEquals("2013-02-21 00:00:00.000", df.format(getFirstInstantOfDay(EUROPE_STOCKHOLM, "2013-02-21")));
		assertEquals("2013-02-21 01:00:00.000", df.format(getFirstInstantOfDay(EUROPE_LONDON, "2013-02-21")));
		assertEquals("2013-02-20 23:00:00.000", df.format(getFirstInstantOfDay(EUROPE_HELSINKI, "2013-02-21")));
	}

	@Test
	public void itShouldTruncateAsExpected() throws ParseException {
		DateFormat df = TimeUtils.createDateAndTimeMillisFormatter(EUROPE_STOCKHOLM);
		Date instant = df.parse("2013-06-15 00:30:00.000"); //at this time, in stockholm

		//there is a day in Stockholm that started at (Stockholm time):
		assertEquals("2013-06-15 00:00:00.000", df.format(truncateInstant(EUROPE_STOCKHOLM, DAY, instant)));
		//there is a day in London (British Summer Time) that started at (Stockholm time):
		assertEquals("2013-06-14 01:00:00.000", df.format(truncateInstant(EUROPE_LONDON, DAY, instant)));
		//there is a day in Helsinki that started at (Stockholm time):
		assertEquals("2013-06-14 23:00:00.000", df.format(truncateInstant(EUROPE_HELSINKI, DAY, instant)));
		//general truncation checks:
		assertEquals("2001-01-01 00:00:00.000", df.format(truncateInstant(EUROPE_STOCKHOLM, YEAR, df.parse("2001-09-05 05:30:12.345"))));
		assertEquals("2001-01-01 00:00:00.000", df.format(truncateInstant(EUROPE_STOCKHOLM, MONTH, df.parse("2001-01-05 05:30:12.345"))));
		assertEquals("2001-01-01 00:00:00.000", df.format(truncateInstant(EUROPE_STOCKHOLM, DAY, df.parse("2001-01-01 05:30:12.345"))));
		assertEquals("2001-01-01 05:00:00.000", df.format(truncateInstant(EUROPE_STOCKHOLM, HOUR, df.parse("2001-01-01 05:30:12.345"))));
		assertEquals("2001-01-01 05:30:00.000", df.format(truncateInstant(EUROPE_STOCKHOLM, MINUTE, df.parse("2001-01-01 05:30:12.345"))));
		assertEquals("2001-01-01 05:30:12.000", df.format(truncateInstant(EUROPE_STOCKHOLM, SECOND, df.parse("2001-01-01 05:30:12.345"))));

	}

	@Test
	public void thereShouldBeNoSystemLocaleMessingThingsUp() throws ParseException {
		DateFormat df = TimeUtils.createDateAndTimeMillisFormatter(UTC);
		Date instant = df.parse("2013-06-15 00:30:00.000"); //at this time, in UTC

		//there is a day in Stockholm that started at (UTC):
		assertEquals("2013-06-14 22:00:00.000", df.format(truncateInstant(EUROPE_STOCKHOLM, DAY, instant)));
		//there is a day in London (British Summer Time, UTC+1) that started at (UTC):
		assertEquals("2013-06-14 23:00:00.000", df.format(truncateInstant(EUROPE_LONDON, DAY, instant)));
		//there is a day in Helsinki that started at (UTC):
		assertEquals("2013-06-14 21:00:00.000", df.format(truncateInstant(EUROPE_HELSINKI, DAY, instant)));
		//general truncation checks:
		assertEquals("2001-01-01 00:00:00.000", df.format(truncateInstant(UTC, YEAR, df.parse("2001-09-05 05:30:12.345"))));
		assertEquals("2001-01-01 00:00:00.000", df.format(truncateInstant(UTC, MONTH, df.parse("2001-01-05 05:30:12.345"))));
		assertEquals("2001-01-01 00:00:00.000", df.format(truncateInstant(UTC, DAY, df.parse("2001-01-01 05:30:12.345"))));
		assertEquals("2001-01-01 05:00:00.000", df.format(truncateInstant(UTC, HOUR, df.parse("2001-01-01 05:30:12.345"))));
		assertEquals("2001-01-01 05:30:00.000", df.format(truncateInstant(UTC, MINUTE, df.parse("2001-01-01 05:30:12.345"))));
		assertEquals("2001-01-01 05:30:12.000", df.format(truncateInstant(UTC, SECOND, df.parse("2001-01-01 05:30:12.345"))));

	}

	@Test
	public void canConvertToUTCThenTruncate() throws Exception {
		DateFormat sthlm = TimeUtils.createDateAndTimeMillisFormatter(EUROPE_STOCKHOLM);

		DateFormat utc = TimeUtils.createDateAndTimeMillisFormatter(UTC);

		utc.format(truncateInstant(UTC, DAY, sthlm.parse("2013-06-15 00:30:00.000")));

		// convert to UTC and truncate
		assertEquals("2013-06-14 00:00:00.000", utc.format(truncateInstant(UTC, DAY, sthlm.parse("2013-06-15 00:30:00.000"))));
		assertEquals("2013-06-15 00:00:00.000", utc.format(truncateInstant(UTC, DAY, sthlm.parse("2013-06-15 14:30:00.000"))));
		assertEquals("2013-06-14 00:00:00.000", utc.format(truncateInstant(UTC, DAY, sthlm.parse("2013-06-14 11:30:00.000"))));
	}

	/**
	 * Test helper method
	 */
	private void verifyDelays(int[] expectedDelays, int[] startAtWeekDays, int applyWorkdaysDelay) {
		Calendar cal = CalendarUtils.createCalendar(EUROPE_STOCKHOLM);
		for (int i = 0; i < expectedDelays.length; i++) {
			int expectedDelay = expectedDelays[i];
			int startWeekDay = startAtWeekDays[i];
			cal.set(DAY_OF_WEEK, startWeekDay);
			Date result = addWorkDays(EUROPE_STOCKHOLM, cal.getTime(), applyWorkdaysDelay);
			assertEquals("Expected a difference of " + expectedDelay + " days when delaying " + cal.getTime() + " with " + applyWorkdaysDelay + " day(s). ", expectedDelay, getNrDaysBetweenDates(cal.getTime(), result));
		}
	}

	/**
	 * Test helper method
	 */
	private static long getNrDaysBetweenDates(Date one, Date two) {
		long diffMillis = two.getTime() - one.getTime();
		return Math.round(((double) diffMillis) / (1000L * 3600L * 24L));
	}
}
