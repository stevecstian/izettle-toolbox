package com.izettle.java;

import java.util.Arrays;

public class StringUtils {
    public static String repeatChar(char subject, int occurances) {
        if (occurances <= 0) {
            return "";
        }
        char[] arr = new char[occurances];
        Arrays.fill(arr, subject);
        return new String(arr);
    }

    /**
     * Will pad a string with the specified character from left (000in)
     *
     * @param in the string to pad
     * @param desiredLength the total length of the resulting string
     * @param padChar the pad to pad with
     * @return the padded string
     */
    public static String padStringFromLeft(String in, int desiredLength, char padChar) {
        return padString(in, desiredLength, padChar, true);
    }

    /**
     * Will pad a string with the specified character from right (in000)
     *
     * @param in the string to pad
     * @param desiredLength the total length of the resulting string
     * @param padChar the pad to pad with
     * @return the padded string
     */
    public static String padStringFromRight(String in, int desiredLength, char padChar) {
        return padString(in, desiredLength, padChar, false);
    }

    /**
     * Will pad a string with the specified character
     *
     * @param in the string to pad
     * @param desiredLength the total length of the resulting string
     * @param padChar the pad to pad with
     * @param fromLeft true if pad from left (000in), false if from right (in000)
     * @return the padded string
     */
    public static String padString(String in, int desiredLength, char padChar, boolean fromLeft) {
        if (in.length() >= desiredLength) {
            return in;
        }
        String padding = repeatChar(padChar, desiredLength - in.length());
        if (fromLeft) {
            return padding + in;
        } else {
            return in + padding;
        }
    }
}
