package com.izettle.java;

import static com.izettle.java.ArrayUtils.concat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ArrayUtilsSpec {

    /**
     * Test of concat method, of class ByteUtils.
     */
    @Test
    public void testConcat_byteArr_byteArr() {

        byte[] expected = new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0, (byte) 0xA5, (byte) 0xB2,
                (byte) 0xFF, (byte) 0xD0};
        byte[] actual = concat(new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0}, new byte[]{
                (byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0});
        assertArrayEquals(expected, actual);
    }

    /**
     * Test of concat method, of class ByteUtils.
     */
    @Test
    public void testConcat_byteArrArr() {

        byte[] expected = new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0, (byte) 0xA5, (byte) 0xB2,
                (byte) 0xFF, (byte) 0xD0};
        byte[] actual = concat();
        assertNull(actual);

        actual = concat(new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0});
        assertArrayEquals(new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0}, actual);
        actual = concat(Arrays.asList(new byte[][]{new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0},
                new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0}}));
        assertArrayEquals(expected, actual);
        expected = new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0, (byte) 0xA5, (byte) 0xB2, (byte) 0xFF,
                (byte) 0xD0, (byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0};
        actual = concat(new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0}, new byte[]{
                (byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0}, new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF,
                (byte) 0xD0});
        assertArrayEquals(expected, actual);
    }

    /**
     * Test of concat method, of class ByteUtils.
     */
    @Test
    public void testConcat_List() {

        List<byte[]> byteArrays = new ArrayList<>();
        byteArrays.add(new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0});
        byteArrays.add(new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0});

        byte[] expected = new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0, (byte) 0xA5, (byte) 0xB2,
                (byte) 0xFF, (byte) 0xD0};
        byte[] actual = concat(byteArrays);

        assertArrayEquals(expected, actual);

    }
}
