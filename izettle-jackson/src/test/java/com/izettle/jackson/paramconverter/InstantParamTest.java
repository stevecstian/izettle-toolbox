package com.izettle.jackson.paramconverter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import javax.ws.rs.BadRequestException;
import org.junit.Test;

public class InstantParamTest {

    private Instant myInstant = Instant.ofEpochMilli(1387920896123L); // 24 Dec 2013, 22:34:56.123 (UTC+1)

    @Test
    public void shouldBeAbleToSerializeAnInstant() throws Exception {
        final String result = new InstantParam(myInstant).toString();
        assertEquals("2013-12-24T21:34:56.123+0000", result);
    }

    @Test
    public void shouldHavePublicStaticValueOfMetod() throws Exception {
        final Method valueOfMethod = InstantParam.class.getMethod("valueOf", String.class);
        assertNotNull(valueOfMethod);
        assertTrue(Modifier.isPublic(valueOfMethod.getModifiers()));
        assertTrue(Modifier.isStatic(valueOfMethod.getModifiers()));
        assertEquals(InstantParam.class, valueOfMethod.getReturnType());
    }

    @Test
    public void shouldBeAbleToDeserializeIncomingRequestParameter() throws Exception {
        final String requestParamValue = "2013-12-24T21:34:56.123+0000";
        final InstantParam result = InstantParam.valueOf(requestParamValue);

        assertEquals(myInstant, result.getInstant());
    }

    @Test
    public void shouldThrowWhenDeserializeFaultyRequestParameter() throws Exception {
        final String requestParamValue = "99-12-24T21:34:56.123+0000";
        try {
            final InstantParam ignored = InstantParam.valueOf(requestParamValue);
            fail("Should not have been able to parse the string: " + requestParamValue);
        } catch (final BadRequestException ex) {
            assertTrue(ex.getCause() instanceof DateTimeParseException);
        }
    }

    @Test
    public void shouldBeAbleToDeserializeIncomingRequestParameterWithZuluIndicator() throws Exception {
        final String requestParamValue = "2013-12-24T21:34:56.123Z";
        final InstantParam result = InstantParam.valueOf(requestParamValue);
        assertEquals(myInstant, result.getInstant());
    }

    @Test
    public void shouldBeAbleToDeserializeIncomingRequestParameterWithoutMillis() throws Exception {
        final String requestParamValue = "2013-12-24T21:34:56+0000";
        final InstantParam result = InstantParam.valueOf(requestParamValue);
        final Instant expected = Instant.parse("2013-12-24T21:34:56Z");
        assertEquals(expected, result.getInstant());
    }

    @Test
    public void shouldBeAbleToDeserializeIncomingRequestParameterWithSomeFractionalSeconds() throws Exception {
        final String requestParamValue = "2013-12-24T21:34:56.21+0000";
        final InstantParam result = InstantParam.valueOf(requestParamValue);
        final Instant expected = Instant.parse("2013-12-24T21:34:56.21Z");
        assertEquals(expected, result.getInstant());
    }

    @Test
    public void shouldBeAbleToDeserializeIncomingRequestParameterWithZuluIndicatorWithoutMillis() throws Exception {
        final String requestParamValue = "2013-12-24T21:34:56Z";
        final InstantParam result = InstantParam.valueOf(requestParamValue);
        final Instant expected = Instant.parse("2013-12-24T21:34:56Z");
        assertEquals(expected, result.getInstant());
    }

}
