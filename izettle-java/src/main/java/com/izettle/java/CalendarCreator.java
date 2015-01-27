package com.izettle.java;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Class for facilitating the creation of Calendars with a proper TimeZoneId
 */
public class CalendarCreator {

	private CalendarCreator() {
	}

	public static Calendar create(TimeZoneId timeZoneId) {
		return Calendar.getInstance(timeZoneId.getTimeZone());
	}

	public static Calendar create(TimeZoneId timeZoneId, Locale locale) {
		return Calendar.getInstance(timeZoneId.getTimeZone(), locale);
	}

	public static Calendar create(Date date, TimeZoneId timeZoneId, Locale locale) {
		Calendar calendar = create(timeZoneId, locale);
		calendar.setTime(date);
		return calendar;
	}
}
