package com.izettle.java;

import java.time.format.DateTimeFormatter;

public class DateTimeFormatters {

    private static final String RFC_3339_OFFSET_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * Create java.time.DateTimeFormatter commonly used when communicating with services over the internet.
     * Has the ability to parse & format TemporalAccessors, such as java.time.Instant
     * Will always output and parse zone with "+0000"-esque format
     * Does _NOT_ parse the Zulu indicator!
     * Example output: "2013-12-24T21:34:56.123+0000".
     */
    public static final DateTimeFormatter RFC_3339_INSTANT = DateTimeFormatter
        .ofPattern(RFC_3339_OFFSET_FORMAT)
        .withZone(TimeZoneId.UTC.toZoneId());

}
