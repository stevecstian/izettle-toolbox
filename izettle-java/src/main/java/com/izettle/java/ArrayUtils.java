package com.izettle.java;

import java.util.List;

public class ArrayUtils {

    private ArrayUtils() {
    }

    public static byte[] concat() {
        return null;
    }

    public static byte[] concat(byte[] a) {
        return a;
    }

    public static byte[] concat(byte[] a, byte[] b) {
        byte[] retArr = new byte[a.length + b.length];
        System.arraycopy(a, 0, retArr, 0, a.length);
        System.arraycopy(b, 0, retArr, a.length, b.length);
        return retArr;
    }

    public static byte[] concat(byte[] a, byte[] b, byte[] c) {
        byte[] retArr = new byte[a.length + b.length + c.length];
        System.arraycopy(a, 0, retArr, 0, a.length);
        System.arraycopy(b, 0, retArr, a.length, b.length);
        System.arraycopy(c, 0, retArr, a.length + b.length, c.length);
        return retArr;
    }

    public static byte[] concat(List<byte[]> concatenees) {
        int totLen = 0;
        for (byte[] concatenee : concatenees) {
            if (concatenee != null) {
                totLen += concatenee.length;
            }
        }
        byte[] retArr = new byte[totLen];
        int offset = 0;
        for (byte[] concatenee : concatenees) {
            if (concatenee != null) {
                System.arraycopy(concatenee, 0, retArr, offset, concatenee.length);
                offset += concatenee.length;
            }
        }
        return retArr;
    }
}
