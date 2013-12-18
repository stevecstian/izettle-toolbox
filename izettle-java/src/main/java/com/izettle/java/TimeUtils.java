package com.izettle.java;

import com.izettle.java.enums.TimeZoneId;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class TimeUtils {

	public static final String FORMAT_YYYYMMDD = "yyyyMMdd";

	private TimeUtils() {
	}

	/**
	 * Returns a String representation of a time interval in milliseconds.
	 *
	 * @param millis
	 *            the time interval in milliseconds
	 * @return a String representation on the form "Xy Xd Xh Xm Xs".
	 */
	public static String msecToHourMinSec(long millis) {
		long years = millis / 31536000000L;
		millis -= 31536000000L * years;
		long days = millis / 86400000;
		millis -= 86400000 * days;
		long hours = millis / 3600000;
		millis -= 3600000 * hours;
		long minutes = millis / 60000;
		millis -= 60000 * minutes;
		long seconds = millis / 1000;
		if (years != 0) {
			if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
				return years + "y";
			}
			return years + "y " + days + "d " + hours + "h " + minutes + "m " + seconds + "s";
		}
		if (days != 0) {
			if (hours == 0 && minutes == 0 && seconds == 0) {
				return days + "d";
			}
			return days + "d " + hours + "h " + minutes + "m " + seconds + "s";
		}
		if (hours != 0) {
			if (minutes == 0 && seconds == 0) {
				return hours + "h";
			}
			return hours + "h " + minutes + "m " + seconds + "s";
		}
		if (minutes != 0) {
			if (seconds == 0) {
				return minutes + "m";
			}
			return minutes + "m " + seconds + "s";
		}
		if (seconds != 0) {
			return seconds + "s";
		}
		return "0s";
	}

	public static DateFormat createFormatter(String pattern, TimeZoneId timeZoneId) {
		DateFormat df = new SimpleDateFormat(pattern);
		df.setTimeZone(timeZoneId.getTimeZone());
		return df;
	}

	public static DateFormat createFormatter(String pattern, Locale locale, TimeZoneId timeZoneId) {
		DateFormat df = new SimpleDateFormat(pattern, locale);
		df.setTimeZone(timeZoneId.getTimeZone());
		return df;
	}

	/**
	 * Will format the provided date to match formatting commonly used in the HTTP protocol. Specifically RFC 1123 is
	 * used in almost all time-related http headers<br>
	 * Ex: "Sun, 19 Jun 2011 22:26:58 GMT"<br>
	 * Consider the implications that the provided date only carries UTC-time...
	 * @param date
	 * @return the formatted string
	 */
	public static String formatRFC1123(Date date) {
		return createFormatter("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US, TimeZoneId.GMT).format(date);
	}

	/**
	 * @param timeZoneId the time zone id of the spectator
	 * @return date formatter with pattern <b>yyyy-MM-dd</b>(ISO-8601 Date).
	 */
	public static DateFormat createDateFormatter(TimeZoneId timeZoneId) {
		return createFormatter("yyyy-MM-dd", timeZoneId);
	}

	/**
	 * @param locale the locale of the spectator
	 * @param timeZoneId the time zone id of the spectator
	 * @return date formatter with pattern <b>yyyy-MM-dd</b>(ISO-8601 Date).
	 */
	public static DateFormat createDateFormatter(Locale locale, TimeZoneId timeZoneId) {
		return createFormatter("yyyy-MM-dd", locale, timeZoneId);
	}

	/**
	 * @param timeZoneId the time zone id of the spectator
	 * @return date formatter with pattern <b>yyyy-MM-dd HH:mm:ss</b>.
	 */
	public static DateFormat createDateAndTimeFormatter(TimeZoneId timeZoneId) {
		return createFormatter("yyyy-MM-dd HH:mm:ss", timeZoneId);
	}

	/**
	 * @param timeZoneId the time zone id of the spectator
	 * @return date formatter with pattern <b>yyyy-MM-dd HH:mm:ss.SSS</b>.
	 */
	public static DateFormat createDateAndTimeMillisFormatter(TimeZoneId timeZoneId) {
		return createFormatter("yyyy-MM-dd HH:mm:ss.SSS", timeZoneId);
	}

	/**
	 * @param timeZoneId the time zone id of the spectator
	 * @return date formatter with pattern <b>yyyy-MM-dd HH:mm</b>.
	 */
	public static DateFormat createDateTimePickerFormatter(TimeZoneId timeZoneId) {
		return createFormatter("yyyy-MM-dd HH:mm", timeZoneId);
	}

	/**
	 * @param locale the locale of the spectator
	 * @param timeZoneId the time zone id of the spectator
	 * @return date formatter with pattern <b>d MMMM yyyy</b>.
	 */
	public static DateFormat createLongDateFormatter(Locale locale, TimeZoneId timeZoneId) {
		return createFormatter("d MMMM yyyy", locale, timeZoneId);
	}

	/**
	 * Create a DateFormat that parses/formats <b>yyyyMMdd</b> date strings, with respect to the passed in time zone id.
	 * @param timeZoneId - desired timezone.
	 * @return
	 */
	public static DateFormat createYYYYMMDDFormatter(TimeZoneId timeZoneId) {
		return createFormatter(FORMAT_YYYYMMDD, timeZoneId);
	}
}
