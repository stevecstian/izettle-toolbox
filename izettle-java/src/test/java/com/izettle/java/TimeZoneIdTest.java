package com.izettle.java;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class TimeZoneIdTest {

    @Test
    public void testGetTimeZoneId() {
        // Verify that each TimeZoneId can be parsed to TimeZone without errors
        Arrays.stream(TimeZoneId.values()).forEach(TimeZoneId::getTimeZone);
    }

    @Test
    public void testGetZoneId() {
        // Verify that each TimeZoneId can be parsed to ZoneId without errors
        Arrays.stream(TimeZoneId.values()).forEach(TimeZoneId::toZoneId);
    }

    @Test
    public void testFromString() {
        for (TimeZoneId timeZoneId : TimeZoneId.values()) {
            //Note: as zones have dual names we might not always end up with the exact same enum instance, but their
            //ZoneId counterpart should be the same
            assertEquals(
                timeZoneId.getTimeZone().toZoneId(),
                TimeZoneId.fromString(timeZoneId.toZoneId().toString()).toZoneId()
            );
        }
    }

}
