package com.izettle.java;

import static com.izettle.java.CalendarTruncator.CalendarField.DAY;
import static com.izettle.java.CalendarTruncator.CalendarField.HOUR;
import static com.izettle.java.CalendarTruncator.CalendarField.MINUTE;
import static com.izettle.java.CalendarTruncator.CalendarField.MONTH;
import static com.izettle.java.CalendarTruncator.CalendarField.SECOND;
import static com.izettle.java.CalendarTruncator.CalendarField.YEAR;
import static com.izettle.java.CalendarTruncator.getFirstInstantOfDay;
import static com.izettle.java.CalendarTruncator.getFirstInstantOfMonth;
import static com.izettle.java.CalendarTruncator.getFirstInstantOfWeek;
import static com.izettle.java.CalendarTruncator.getFirstInstantOfYear;
import static com.izettle.java.CalendarTruncator.truncateInstant;
import static com.izettle.java.TimeZoneId.EUROPE_HELSINKI;
import static com.izettle.java.TimeZoneId.EUROPE_LONDON;
import static com.izettle.java.TimeZoneId.EUROPE_STOCKHOLM;
import static com.izettle.java.TimeZoneId.UTC;
import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import org.junit.Test;

public class CalendarTruncatorSpec {

	@Test
	public void itShouldReturnCorrectFirstYearlyInstant() {
		DateFormat df = DateFormatCreator.createDateAndTimeMillisFormatter(EUROPE_STOCKHOLM);
		assertEquals("2013-01-01 00:00:00.000", df.format(getFirstInstantOfYear(EUROPE_STOCKHOLM, 2013)));
		assertEquals("2013-01-01 01:00:00.000", df.format(getFirstInstantOfYear(EUROPE_LONDON, 2013)));
		assertEquals("2012-12-31 23:00:00.000", df.format(getFirstInstantOfYear(EUROPE_HELSINKI, 2013)));
	}

	@Test
	public void itShouldReturnCorrectFirstMonthlyInstant() {
		DateFormat df = DateFormatCreator.createDateAndTimeMillisFormatter(EUROPE_STOCKHOLM);
		assertEquals("2013-02-01 00:00:00.000", df.format(getFirstInstantOfMonth(EUROPE_STOCKHOLM, 2013, 2)));
		assertEquals("2013-02-01 01:00:00.000", df.format(getFirstInstantOfMonth(EUROPE_LONDON, 2013, 2)));
		assertEquals("2013-01-31 23:00:00.000", df.format(getFirstInstantOfMonth(EUROPE_HELSINKI, 2013, 2)));
		//Testing roll over
		assertEquals("2014-01-01 00:00:00.000", df.format(getFirstInstantOfMonth(EUROPE_STOCKHOLM, 2013, 13)));
	}

	@Test
	public void itShouldReturnCorrectFirstWeeklyInstant() {
		DateFormat df = DateFormatCreator.createDateAndTimeMillisFormatter(EUROPE_STOCKHOLM);
		assertEquals("2013-04-08 00:00:00.000", df.format(getFirstInstantOfWeek(EUROPE_STOCKHOLM, 2013, 15)));
		assertEquals("2013-04-08 01:00:00.000", df.format(getFirstInstantOfWeek(EUROPE_LONDON, 2013, 15)));
		assertEquals("2013-04-07 23:00:00.000", df.format(getFirstInstantOfWeek(EUROPE_HELSINKI, 2013, 15)));
	}

	@Test
	public void itShouldReturnCorrectFirstInstantOfDateString() throws ParseException {
		DateFormat df = DateFormatCreator.createDateAndTimeMillisFormatter(EUROPE_STOCKHOLM);
		assertEquals("2013-02-21 00:00:00.000", df.format(getFirstInstantOfDay(EUROPE_STOCKHOLM, "2013-02-21")));
		assertEquals("2013-02-21 01:00:00.000", df.format(getFirstInstantOfDay(EUROPE_LONDON, "2013-02-21")));
		assertEquals("2013-02-20 23:00:00.000", df.format(getFirstInstantOfDay(EUROPE_HELSINKI, "2013-02-21")));
	}

	@Test
	public void itShouldTruncateAsExpected() throws ParseException {
		DateFormat df = DateFormatCreator.createDateAndTimeMillisFormatter(EUROPE_STOCKHOLM);
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
		DateFormat df = DateFormatCreator.createDateAndTimeMillisFormatter(UTC);
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
		DateFormat sthlm = DateFormatCreator.createDateAndTimeMillisFormatter(EUROPE_STOCKHOLM);

		DateFormat utc = DateFormatCreator.createDateAndTimeMillisFormatter(UTC);

		utc.format(truncateInstant(UTC, DAY, sthlm.parse("2013-06-15 00:30:00.000")));

		// convert to UTC and truncate
		assertEquals("2013-06-14 00:00:00.000", utc.format(truncateInstant(UTC, DAY, sthlm.parse("2013-06-15 00:30:00.000"))));
		assertEquals("2013-06-15 00:00:00.000", utc.format(truncateInstant(UTC, DAY, sthlm.parse("2013-06-15 14:30:00.000"))));
		assertEquals("2013-06-14 00:00:00.000", utc.format(truncateInstant(UTC, DAY, sthlm.parse("2013-06-14 11:30:00.000"))));
	}
}
