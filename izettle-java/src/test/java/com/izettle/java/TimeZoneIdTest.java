package com.izettle.java;

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

}
