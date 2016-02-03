package com.izettle.java.compat;

import com.izettle.java.compat.TimeZoneId;
import com.izettle.java.compat.CalendarCreator;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.junit.Test;

public class CalendarCreatorTest {

    @Test
    public void shouldCreateCalendarAndSetDate() throws Exception {
        // Arrange
        Date date = new Date(1402518597L); // 6/11/2014 10:29:57 PM GMT+2
        TimeZoneId timeZoneId = TimeZoneId.EUROPE_STOCKHOLM;
        Locale locale = new Locale("sv_SE");

        // Action
        Calendar calendar = CalendarCreator.create(date, timeZoneId, locale);

        // Assert
        assertEquals(1402518597L, calendar.getTimeInMillis());
        assertEquals("Europe/Stockholm", calendar.getTimeZone().getID());
    }
}
