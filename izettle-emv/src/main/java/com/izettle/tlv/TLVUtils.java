package com.izettle.tlv;

import static com.izettle.java.ValueChecks.empty;

/**
 * Created by fidde on 17/12/14.
 */
public class TLVUtils {

	private TLVUtils() {}

	static int tagToInt(byte[] tag) throws TLVException {

		validateTag(tag);

		if (tag.length > 4) {
			throw new TLVException("Cannot convert tag more than 4 bytes long into an integer");
		}

		int tagAsInteger = 0;
		for (int i = 0; i < tag.length; i++) {
			tagAsInteger |= (tag[i] & 0xff) << (8 * (tag.length - i - 1));
		}
		return tagAsInteger;
	}

	static void validateTag(byte[] tag) throws TLVException {

		if (empty(tag)) {
			throw new TLVException("Malformed tag: empty");
		}

		/*
			 UNUSED: Leading byte, B8 + B7 is application class
			 UNUSED: Leading byte, B6 is primitive/constructed flag
		 */

		boolean isMultiByteTag = (tag[0] & 0x1f) == 0x1f;

		if (isMultiByteTag) {
			if (1 == tag.length) {
				throw new TLVException("Malformed tag: indicates multibyte, but is not");
			}
			if (0x1f == (tag[tag.length - 1] & 0x1f)) {
				throw new TLVException("Malformed tag: multibyte, but last byte doesn't close");
			}
			for (int i = 1; i < tag.length - 1; i++) {
				if (0x80 != (tag[i] & 0x80)) {
					throw new TLVException("Malformed tag: multibyte, but 0x80 not set in tag byte " + i);
				}
			}
		} else {
			if (1 != tag.length) {
				throw new TLVException("Malformed tag: indicates single byte, but is not");
			}
		}
	}

}
