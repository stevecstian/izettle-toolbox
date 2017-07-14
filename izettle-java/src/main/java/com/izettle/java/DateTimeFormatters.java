package com.izettle.java;

import java.time.format.DateTimeFormatter;

public class DateTimeFormatters {

    /**
     * Create java.time.DateTimeFormatter commonly used when communicating with services over the internet.
     * Has the ability to parse & format TemporalAccessors, such as java.time.Instant
     * Will always output and parse zone with "+0000"-esque format
     * Does also parse the Zulu indicator to make life easier for some clients
     * Example output: "2013-12-24T21:34:56.123+0000".
     */
    public static final DateTimeFormatter RFC_3339_INSTANT = DateTimeFormatter
        .ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSZ")
        .withZone(TimeZoneId.UTC.toZoneId());

    /**
     * Formatter used for parsing. Should not be used for serialization
     */
    public static final DateTimeFormatter INSTANT_WITH_ZULU_OR_OFFSET = DateTimeFormatter
        .ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSXX");

    /**
     * Formatter used for parsing. Should not be used for serialization
     */
    public static final DateTimeFormatter INSTANT_WITH_NO_MILLIS_FALLBACK = DateTimeFormatter
        .ofPattern("uuuu-MM-dd'T'HH:mm:ssX");


}
