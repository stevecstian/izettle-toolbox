package com.izettle.tlv;

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
        helper(in, 0, out);
        return out;
    }

    private void helper(byte[] input, int offset, List<TLV> tags) throws TLVException {

        if (!strictMode) {
            // Remove leading zeroes
            while (offset < input.length && 0 == input[offset]) {
                offset++;
            }
        }

        if (offset == input.length) {
            return;
        }

        // Parse tag
        byte[] tag = new byte[]{input[offset]};
        if ((input[offset] & 0x1f) == 0x1f) {
            /*
             * If first byte of a tag has lowest 5 bits set, it's a multi-byte
             * tag. Subsequent tag bytes have 0x80 bit set.
             */
            do {
                tag = ArrayUtils.concat(tag, new byte[]{input[++offset]});
            } while ((input[offset] & 0x80) == 0x80);
        }

        // Validate tag
        TLVUtils.validateTag(tag);

        if (offset + 1 >= input.length) {
            throw new TLVException("Malformed data: Tag, but no length present");
        }

        int length = input[++offset];
        byte[] lengthEncoded;

        if ((length & 0x80) == 0x80) {

            int numBytesForLength = length ^ (byte) 0x80;

            // Save the actual encoded length
            lengthEncoded = new byte[numBytesForLength + 1];
            if (numBytesForLength + offset > input.length) {
                throw new TLVException("Malformed length, first length byte indiciates length that doesn't fit");
            }
            System.arraycopy(input, offset, lengthEncoded, 0, numBytesForLength + 1);

            length = 0;
            while (numBytesForLength-- > 0) {
                length |= (input[++offset] & 0xff) << (numBytesForLength * 8);
            }
        } else {
            lengthEncoded = new byte[]{(byte) length};
        }

        ++offset; // Now positioned at first data byte

        if (offset + length > input.length) {
            throw new TLVException("Tag " + Hex.toHexString(tag) + " exceeds data length");
        }

        byte[] value = new byte[length];
        System.arraycopy(input, offset, value, 0, length);

        int tagAsInteger = TLVUtils.tagToInt(tag);

        if (expandTags.contains(tagAsInteger)) {
            if (offset + value.length > input.length) {
                throw new TLVException("Expand tag " + Hex.toHexString(tag) + " is invalid, exceeds data length");
            }
            helper(value, 0, tags);
            if (offset + length < input.length) {
                // There are more tags after the expander tag.
                helper(input, offset + length, tags);
            }
        } else {
            tags.add(new TLV(tag, lengthEncoded, value));
            if (offset + length == input.length) {
                // We are finished
            } else {
                helper(input, offset + length, tags);
            }
        }
    }
}
