package com.izettle.java.compat;

public class Bits {

    public static String toBitString(byte b) {
        return toBitString(new byte[]{b});
    }

    public static String toBitString(byte[] b) {
        boolean[] bitArr = toBitArray(b);
        StringBuilder sb = new StringBuilder(bitArr.length);
        for (boolean aBitArr : bitArr) {
            sb.append(aBitArr ? 1 : 0);
        }
        return sb.toString();
    }

    public static boolean[] toBitArray(byte[] bytes) {
        boolean[] bits = new boolean[bytes.length * 8];
        int p = 0;
        for (int i = 0; i < bytes.length; i++, p += 8) {
            bits[p + 0] = (bytes[i] & 0x80) > 0;
            bits[p + 1] = (bytes[i] & 0x40) > 0;
            bits[p + 2] = (bytes[i] & 0x20) > 0;
            bits[p + 3] = (bytes[i] & 0x10) > 0;
            bits[p + 4] = (bytes[i] & 0x08) > 0;
            bits[p + 5] = (bytes[i] & 0x04) > 0;
            bits[p + 6] = (bytes[i] & 0x02) > 0;
            bits[p + 7] = (bytes[i] & 0x01) > 0;
        }
        return bits;
    }

    public static byte[] intTo4BytesBigEndian(int i) {
        byte[] retByte = new byte[4];
        retByte[0] = (byte) (i >>> 24 & 0xFF);
        retByte[1] = (byte) (i >>> 16 & 0xFF);
        retByte[2] = (byte) (i >>> 8 & 0xFF);
        retByte[3] = (byte) (i & 0xFF);
        return retByte;
    }
}
