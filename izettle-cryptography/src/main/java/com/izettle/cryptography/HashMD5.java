package com.izettle.cryptography;

import static com.izettle.java.Base64.byteArrToB64String;
import static com.izettle.java.ValueChecks.empty;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

public abstract class HashMD5 {

    private HashMD5() {
    }

    public static String digestStringsToB64Hash(List<String> subjects) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.reset();
            for (String string : subjects) {
                if (!empty(string)) {
                    md.update(string.getBytes("utf-8"));
                }
            }
            byte[] thedigest = md.digest();
            return byteArrToB64String(thedigest);
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String digestStringsToB64Hash(String... subjects) {
        return digestStringsToB64Hash(Arrays.asList(subjects));
    }
}
