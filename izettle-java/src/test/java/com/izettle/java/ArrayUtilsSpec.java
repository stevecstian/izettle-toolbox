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

        byte[] expected = {
            (byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0, (byte) 0xA5, (byte) 0xB2,
            (byte) 0xFF, (byte) 0xD0
        };
        byte[] actual = concat(new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0}, new byte[]{
            (byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0
        });
        assertArrayEquals(expected, actual);
    }

    /**
     * Test of concat method, of class ByteUtils.
     */
    @Test
    public void testConcat_byteArrArr() {

        byte[] expected1 = {
            (byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0, (byte) 0xA5, (byte) 0xB2,
            (byte) 0xFF, (byte) 0xD0
        };
        byte[] actual1 = concat();
        assertNull(actual1);

        byte[] actual2 = concat(new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0});
        assertArrayEquals(new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0}, actual2);
        byte[] actual3 = concat(Arrays.asList(new byte[][]{
            new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0},
            new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0}
        }));
        assertArrayEquals(expected1, actual3);
        byte[] expected2 = new byte[]{
            (byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0, (byte) 0xA5, (byte) 0xB2, (byte) 0xFF,
            (byte) 0xD0, (byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0
        };
        byte[] actual4 = concat(new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0}, new byte[]{
            (byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0
        }, new byte[]{
            (byte) 0xA5, (byte) 0xB2, (byte) 0xFF,
            (byte) 0xD0
        });
        assertArrayEquals(expected2, actual4);
    }

    /**
     * Test of concat method, of class ByteUtils.
     */
    @Test
    public void testConcat_List() {

        List<byte[]> byteArrays = new ArrayList<>();
        byteArrays.add(new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0});
        byteArrays.add(new byte[]{(byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0});

        byte[] expected = {
            (byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0, (byte) 0xA5, (byte) 0xB2,
            (byte) 0xFF, (byte) 0xD0
        };
        byte[] actual = concat(byteArrays);

        assertArrayEquals(expected, actual);

    }
}
