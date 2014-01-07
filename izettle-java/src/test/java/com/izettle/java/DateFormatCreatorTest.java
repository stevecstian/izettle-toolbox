package com.izettle.java;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import org.junit.Test;

public class DateFormatCreatorTest {

	private final Date dateToVerify = new Date(1387920896123L); // 24 Dec 2013, 22:34:56.123 (UTC+1)

	@Test
	public void shouldFormatDatesAsISO8601InUTC() throws Exception {
		String formattedDate = DateFormatCreator.createISO8601Formatter().format(dateToVerify);
		assertEquals("2013-12-24T21:34:56.123+0000", formattedDate);
	}
}
