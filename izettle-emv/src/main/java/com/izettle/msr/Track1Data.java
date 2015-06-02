package com.izettle.msr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fidde on 02/06/15.
 */
public class Track1Data {

    private static final Pattern P = Pattern.compile(
        ""
            + "%([A-Z])([0-9]{1,19})\\^([^\\^]{2,26})\\^([0-9]{4}|\\^)"
            + "([0-9]{3}|\\^)([^\\?]+)\\?.*"
    );

    public String formatCode;
    public String pan;
    public String name;
    public String expirationDate;
    public String serviceCode;
    public String discretionaryData;
    public String raw;

    public static Track1Data parse(String ascii) {

        Track1Data out = new Track1Data();
        out.raw = ascii;

        Matcher m = P.matcher(ascii);
        if (m.matches()) {
            out.formatCode = m.group(1);
            out.pan = m.group(2);
            out.name = m.group(3);
            out.expirationDate = m.group(4);
            out.serviceCode = m.group(5);
            out.discretionaryData = m.group(6);
        }
        return out;
    }

}
