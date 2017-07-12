package com.izettle.jackson.paramconverter;

import static com.izettle.java.DateTimeFormatters.RFC_3339_INSTANT;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import javax.ws.rs.BadRequestException;

/**
 * Used as a <b>@QueryParam</b> to wrap an Instant. The URI string representation should be in RFC3339 format
 * Can also be used with to wrap an Instant with usage in <b>@PathParam</b>
 */

public class InstantParam {

    private final Instant instant;

    public InstantParam(final Instant instant) {
        this.instant = instant;
    }

    public static InstantParam valueOf(final String rfc3339DateString) throws BadRequestException {
        try {
            final Instant instant = RFC_3339_INSTANT.parse(rfc3339DateString, Instant::from);
            return new InstantParam(instant);
        } catch (final DateTimeParseException e) {
            throw new BadRequestException(e);
        }
    }

    public Instant getInstant() {
        return instant;
    }

    @Override
    public String toString() {
        return RFC_3339_INSTANT.format(instant);
    }
}
