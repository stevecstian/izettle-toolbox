package com.izettle.tlv;

import com.izettle.java.ArrayUtils;
import com.izettle.java.Hex;

import java.util.*;

/**
 * ISO 7816 / ASN.1 compliantish decoder.
 * @author fidde
 */
public class TLVDecoder {

	private Set<Integer> expandTags = new HashSet<>();

	public TLVDecoder() {

	}

	public List<TLV> parse(byte[] in) throws TLVException {

		List<TLV> out = new ArrayList<>();
		helper(in, 0, out);
		return out;
	}

	public static void main(String[] args) {
		//List<TLV> tags = new ArrayList<>();
		//byte[] input = Hex.hexToByteArray("DFFF03");
		//helper(input, 0, tags);
		try {
			TLVEncoder encoder = new TLVEncoder();
			TLV tlv = encoder.encode(Hex.hexToByteArray("9F26"), new byte[128]);

			byte[] raw = ArrayUtils.concat(tlv.getTag(), tlv.getLength(), tlv.getValue());
			TLVDecoder decoder = new TLVDecoder();
			decoder.parse(raw);

		} catch(Exception e) {
			e.printStackTrace();;
		}
	}

	public void helper(byte[] input, int offset, List<TLV> tags) throws TLVException {
		/*
		 * Parse tag
		 */
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

		System.out.println("Parsed tag: " + Hex.toHexString(tag));

		if (offset + 1 >= input.length) {
			return;
		}

		int length = input[++offset];
		byte[] lengthEncoded;

		if ((length & 0x80) == 0x80) {
			int numBytesForLength = length ^ (byte) 0x80;
			lengthEncoded = new byte[numBytesForLength];
			System.arraycopy(input, offset, lengthEncoded, 0, numBytesForLength);
			length = 0;
			while (numBytesForLength-- > 0) {
				length |= (input[++offset] & 0xFF) << (numBytesForLength * 8);
			}
		} else {
			lengthEncoded = new byte[]{(byte)length};
		}


		System.out.println("Parsed length " + length);

		++offset; // Now positioned at first data byte

		if (offset + length > input.length) {
			return;
		}

		byte[] value = new byte[length];
		System.arraycopy(input, offset, value, 0, length);

		int tagAsInteger = 0;
		for(int i=0; i<tag.length; i++) {
			tagAsInteger |= (tag[i] & 0xff) << (8 * (tag.length - i - 1));
		}

		if(expandTags.contains(tagAsInteger)) {
			helper(value, 0, tags);
		} else {
			tags.add(new TLV(tag, lengthEncoded, value));
		}


		/*
		if (wrapperTagsHex.contains(tag.tagAsHex())) {
			helper(tag.dataBytes, 0, tags, wrapperTagsHex);
		} else {
			tags.add(tag);
		}
		*/



	}

}
