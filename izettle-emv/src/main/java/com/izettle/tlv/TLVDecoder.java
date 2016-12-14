package com.izettle.tlv;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * ISO 7816 / ASN.1 compliantish decoder.
 * @author fidde
 */
public class TLVDecoder {

    private final Set<Integer> expandTags = new HashSet<>();
    private boolean strictMode;

    public TLVDecoder() {
    }

    /**
     * Enable strict mode, which does not allow (non-TLV formatted) zero padding between or after
     * the TLV elements. Default is <b>false</b>
     */
    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    public void addExpandTag(byte[] tag) throws TLVException {
        expandTags.add(TLVUtils.tagToInt(tag));
    }

    public List<TLV> decode(byte[] in) throws TLVException {

        List<TLV> out = new ArrayList<>();

        final ByteBuffer wrap = ByteBuffer.wrap(in);

        addAllTags(wrap, in.length, out);

        return out;
    }

    private void addAllTags(ByteBuffer input, long maxLength, List<TLV> out) throws TLVException {
        while (input.position() < input.capacity() && input.position() < maxLength) {
            helper(input, out);
        }
    }

    private void helper(ByteBuffer input, List<TLV> tags) throws TLVException {
        if (!strictMode) {
            // Remove leading zeroes
            byte leading = input.get();
            input.position(input.position() - 1);

            while (leading == 0 && input.position() < input.capacity()) {
                leading = input.get();
            }
        }

        if (input.position() >= input.capacity()) {
            return;
        }

        int tagStart = input.position();
        int tagEnd = tagStart + 1;
        if ((input.get() & 0x1f) == 0x1f) {
            do {
                ++tagEnd;
            } while ((input.get() & 0x80) == 0x80);
        }

        // Validate tag
        input.position(tagStart);
        final byte[] tag = new byte[tagEnd - tagStart];
        input.get(tag);
        TLVUtils.validateTag(tag);

        if (input.position() >= input.capacity()) {
            throw new TLVException("Malformed data: Tag, but no length present");
        }

        int length = input.get();
        byte[] lengthEncoded;

        if ((length & 0x80) == 0x80) {

            int numBytesForLength = length ^ (byte) 0x80;

            // Save the actual encoded length
            lengthEncoded = new byte[numBytesForLength + 1];
            if (numBytesForLength + input.position() > input.capacity()) {
                throw new TLVException("Malformed length, first length byte indiciates length that doesn't fit");
            }
            input.position(input.position() - 1);
            int preOffset = input.position();
            input.get(lengthEncoded);
            input.position(preOffset + 1);

            length = 0;
            while (numBytesForLength-- > 0) {
                length |= (input.get() & 0xff) << (numBytesForLength * 8);
            }
        } else {
            lengthEncoded = new byte[]{(byte) length};
        }

        if (input.position() + length > input.capacity()) {
            throw new TLVException("Tag " + Hex.toHexString(tag) + " exceeds data length");
        }

        if (length >= Integer.MAX_VALUE) {
            throw new TLVException("Tag " + Hex.toHexString(tag) + " exceeds max data length");
        }

        int tagAsInteger = TLVUtils.tagToInt(tag);

        if (expandTags.contains(tagAsInteger)) {
            addAllTags(input, length, tags);
        } else {
            byte[] value = new byte[length];
            input.get(value);

            tags.add(new TLV(tag, lengthEncoded, value));
        }
    }
}
