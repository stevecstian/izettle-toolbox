package com.izettle.tlv;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

public class TLV {

    private final byte[] tag;
    private final byte[] length;
    private final byte[] value;

    public TLV(byte[] tag, byte[] length, byte[] value) {
        this.tag = requireNonNull(tag, "Tag cannot be null");
        this.length = requireNonNull(length, "Length cannot be null");
        this.value = requireNonNull(value, "Value cannot be null");
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
    }
}
