package com.izettle.tlv;

import static com.izettle.java.ValueChecks.anyNull;

import com.izettle.java.ArrayUtils;
import com.izettle.java.Hex;
import java.util.Arrays;

/**
 * Created by fidde on 16/12/14.
 */
public class TLV {

    private final byte[] tag;
    private final byte[] length;
    private final byte[] value;

    public TLV(byte[] tag, byte[] length, byte[] value) {
        if (anyNull(tag, length, value)) {
            throw new IllegalStateException("No null arguments");
        }
        this.tag = tag;
        this.length = length;
        this.value = value;
    }

    public byte[] getTag() {
        return Arrays.copyOf(tag, tag.length);
    }

    public byte[] getLength() {
        return Arrays.copyOf(length, length.length);
    }

    public byte[] getValue() {
        return Arrays.copyOf(value, value.length);
    }

    public byte[] toBytes() {
        return ArrayUtils.concat(tag, length, value);
    }

    @Override
    public String toString() {
        return String.format(
            "[T:%s L:%s V:%s]",
            Hex.toHexString(tag),
            Hex.toHexString(length),
            Hex.toHexString(value));
    };
}
