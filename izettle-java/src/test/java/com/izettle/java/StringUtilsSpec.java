package com.izettle.java;

import static com.izettle.java.StringUtils.padStringFromLeft;
import static com.izettle.java.StringUtils.padStringFromRight;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StringUtilsSpec {
    @Test
    public void itShouldPadLeft() {
        assertEquals("yyyy", padStringFromLeft("", 4, 'y'));
        assertEquals("yyytest", padStringFromLeft("test", 7, 'y'));
        assertEquals("test", padStringFromLeft("test", 4, 'y'));
        assertEquals("test", padStringFromLeft("test", 0, 'y'));
    }

    @Test
    public void itShouldPadRight() {
        assertEquals("yyyy", padStringFromRight("", 4, 'y'));
        assertEquals("testyyy", padStringFromRight("test", 7, 'y'));
        assertEquals("test", padStringFromRight("test", 4, 'y'));
        assertEquals("test", padStringFromRight("test", 0, 'y'));
    }
}
