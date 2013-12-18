package com.izettle.java;

import static com.izettle.java.TimeUtils.msecToHourMinSec;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class TimeUtilsSpec {

	private static final long YEAR = TimeUnit.DAYS.toMillis(365);

	@Test
	public void microsecond() {
		assertEquals("0s", msecToHourMinSec(TimeUnit.MICROSECONDS.toMillis(1)));
	}

	@Test
	public void millisecond() {
		assertEquals("0s", msecToHourMinSec(TimeUnit.MILLISECONDS.toMillis(1)));
	}

	@Test
	public void second() {
		assertEquals("1s", msecToHourMinSec(TimeUnit.SECONDS.toMillis(1)));
	}

	@Test
	public void minute() {
		assertEquals("1m", msecToHourMinSec(TimeUnit.MINUTES.toMillis(1)));
	}

	@Test
	public void hour() {
		assertEquals("1h", msecToHourMinSec(TimeUnit.HOURS.toMillis(1)));
	}

	@Test
	public void day() {
		assertEquals("1d", msecToHourMinSec(TimeUnit.DAYS.toMillis(1)));
	}

	@Test
	public void week() {
		assertEquals("7d", msecToHourMinSec(TimeUnit.DAYS.toMillis(7)));
	}

	@Test
	public void month() {
		assertEquals("30d", msecToHourMinSec(TimeUnit.DAYS.toMillis(30)));
	}

	@Test
	public void year() {
		assertEquals("1y", msecToHourMinSec(YEAR));
	}

	@Test
	public void yearDayHourSecond() {
		assertEquals("1y 1d 1h 0m 1s", msecToHourMinSec(YEAR + TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(1)
				+ TimeUnit.SECONDS.toMillis(1)));
	}

	@Test
	public void yearDayMinuteSecond() {
		assertEquals("1y 1d 0h 1m 1s", msecToHourMinSec(YEAR + TimeUnit.DAYS.toMillis(1) + TimeUnit.MINUTES.toMillis(1)
				+ TimeUnit.SECONDS.toMillis(1)));
	}

	@Test
	public void yearHourMinuteSecond() {
		assertEquals(
				"1y 0d 1h 1m 1s",
				msecToHourMinSec(YEAR + +TimeUnit.HOURS.toMillis(1) + TimeUnit.MINUTES.toMillis(1)
						+ TimeUnit.SECONDS.toMillis(1)));
	}

	@Test
	public void yearDayHourMinuteSecond() {
		assertEquals("1y 1d 1h 1m 1s", msecToHourMinSec(YEAR + TimeUnit.DAYS.toMillis(1) + TimeUnit.HOURS.toMillis(1)
				+ TimeUnit.MINUTES.toMillis(1) + TimeUnit.SECONDS.toMillis(1)));
	}
}
