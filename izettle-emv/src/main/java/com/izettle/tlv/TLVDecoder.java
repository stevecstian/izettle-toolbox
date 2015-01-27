package com.izettle.tlv;

import com.izettle.java.ArrayUtils;
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

    public TLVDecoder() { }

    public void addExpandTag(byte[] tag) throws TLVException {
        expandTags.add(TLVUtils.tagToInt(tag));
    }

    public List<TLV> decode(byte[] in) throws TLVException {

        List<TLV> out = new ArrayList<>();
        helper(in, 0, out);
        return out;
    }

    private void helper(byte[] input, int offset, List<TLV> tags) throws TLVException {

        // Parse tag
        byte[] tag = new byte[]{input[0]};
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
            return;
        }

        int length = input[++offset];
        byte[] lengthEncoded;

        if ((length & 0x80) == 0x80) {

            int numBytesForLength = length ^ (byte) 0x80;

            // Save the actual encoded length
            lengthEncoded = new byte[numBytesForLength + 1];
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
            return;
        }

        byte[] value = new byte[length];
        System.arraycopy(input, offset, value, 0, length);

        int tagAsInteger = TLVUtils.tagToInt(tag);

        if (expandTags.contains(tagAsInteger)) {
            helper(value, 0, tags);
        } else {
            tags.add(new TLV(tag, lengthEncoded, value));
        }
    }
}
