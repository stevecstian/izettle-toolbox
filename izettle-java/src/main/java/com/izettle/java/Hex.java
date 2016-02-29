package com.izettle.java;

public class Hex {

    private static final char[] BASE_16_DIGITS = "0123456789ABCDEF".toCharArray();

    /**
     * Will return an uppercase hex representation of the provided byte array.
     *
     * @param byteArr the source of bytes
     * @return a string of uppercase hex characters, twice as long as the byte array
     */
    public static String toHexString(byte[] byteArr) {
        if (byteArr == null) {
            return "";
        }
        char[] result = new char[2 * byteArr.length];
        for (int i = 0; i < byteArr.length; i++) {
            byte b1 = (byte) ((byteArr[i] >> 4) & 0x0000000F);
            byte b2 = (byte) (byteArr[i] & 0x0000000F);
            result[2 * i] = BASE_16_DIGITS[b1];
            result[2 * i + 1] = BASE_16_DIGITS[b2];
        }
        return String.valueOf(result);
    }

    public static String toHexString(byte b) {
        char[] result = new char[2];
        result[0] = BASE_16_DIGITS[(b >> 4) & 0x0000000F];
        result[1] = BASE_16_DIGITS[b & 0x0000000F];
        return String.valueOf(result);
    }

    public static byte[] hexToByteArray(String hexString) {
        if (hexString == null) {
            return null;
        }
        byte[] retArr = new byte[hexString.length() / 2];
        for (int i = 0; i < retArr.length; i++) {
            retArr[i] = hexToByte(hexString.substring(i * 2, i * 2 + 2));
        }
        return retArr;
    }

    public static byte hexToByte(String hex) {
        return (byte) Integer.parseInt(hex, 16);
    }
}
