package com.izettle.java;

import com.izettle.java.enums.TimeZoneId;
import java.util.Calendar;
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
}
