package com.izettle.java;

public class Base64 {

    static final char[] BASE_64_DIGITS =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();
    static final int[] BASE_64_VALUES = new int['z' + 1];

    static {
        for (int i = 0; i < BASE_64_DIGITS.length; i++) {
            BASE_64_VALUES[BASE_64_DIGITS[i]] = i;
        }
    }

    private Base64() {
    }

    /**
     * Will translate a byte array to it's b64 counterpart. The alphabet used is A-Za-z0-9-_. The result will not be
     * chunked. This method will produce the exact same output as
     * org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString, but twice as fast
     * @param byteArr
     * @return the base 64 encoded string
     */
    public static String byteArrToB64String(byte[] byteArr) {
        if (byteArr == null) {
            return null;
        }
        if (byteArr.length == 0) {
            return "";
        }
        char[] cArr = new char[(int) Math.ceil(((float) (byteArr.length * 4)) / 3)];
        int cIdx = 0;
        int bitOffset = 0;
        int byteOffset = 0;
        while (byteOffset < byteArr.length) {
            int firstByteVal = byteArr[byteOffset] & 0xff;
            int tmpVal = 0;
            switch (bitOffset) {
                case 0: {
                    byte bm = (byte) 0xFF << 2; //"11111100";
                    tmpVal = (firstByteVal & bm) >> 2;
                    break;
                }
                case 6: {
                    byte bm = 0xFF >> 6; //"00000011";
                    tmpVal = (firstByteVal & bm) << 4;
                    if (byteOffset + 1 < byteArr.length) {
                        byte bm2 = (byte) (0xFF << 4); //"11110000";
                        tmpVal |= (byteArr[byteOffset + 1] & 0xff & bm2) >> 4;
                    }
                    break;
                }
                case 4: {
                    byte bm = 0xFF >> 4; //"00001111";
                    tmpVal = (firstByteVal & bm) << 2;
                    if (byteOffset + 1 < byteArr.length) {
                        byte bm2 = (byte) (0xFF << 6); //"11000000";
                        tmpVal |= (byteArr[byteOffset + 1] & 0xff & bm2) >> 6;
                    }
                    break;
                }
                case 2:
                    byte bm = 0xFF >> 2; //"00111111";
                    tmpVal = firstByteVal & bm;
                    break;
            }
            if (bitOffset != 0) {
                byteOffset++;
            }
            bitOffset = (bitOffset + 6) % 8;
            cArr[cIdx++] = BASE_64_DIGITS[tmpVal];
        }
        return new String(cArr);
    }

    /**
     * Will translate (decode) a b64 string to it's byte array conterpart. The alphabet used is A-Za-z0-9-_. The
     * input string
     * must not be chunked. This method will produce the exact same output as
     * org.apache.commons.codec.binary.Base64.decodeBase64, but twice as fast
     * @param b64 the encoded string
     * @return the resulting byte array
     */
    public static byte[] b64StringToByteArr(String b64) {
        if (b64 == null) {
            return null;
        }
        if (b64.isEmpty()) {
            return new byte[]{};
        }
        int len = b64.length();
        while (b64.charAt(len - 1) == '=') {
            len--;
        }
        int qIdx = 0;
        int bitOffset = 0;
        byte[] bArr = new byte[len * 3 / 4];
        int bIdx = 0;
        while (qIdx + 1 < len) {
            int q1 = BASE_64_VALUES[b64.charAt(qIdx)];
            int q2 = BASE_64_VALUES[b64.charAt(qIdx + 1)];
            switch (bitOffset) {
                case 0:
                    bArr[bIdx++] = (byte) ((q1 << 2) | (q2 >> 4));
                    bitOffset = 2;
                    qIdx += 1;
                    break;
                case 2:
                    bArr[bIdx++] = (byte) ((q1 << 4) | (q2 >> 2));
                    bitOffset = 4;
                    qIdx += 1;
                    break;
                case 4:
                    bArr[bIdx++] = (byte) ((q1 << 6) | q2);
                    bitOffset = 0;
                    qIdx += 2;
                    break;
            }
        }
        return bArr;
    }
}
