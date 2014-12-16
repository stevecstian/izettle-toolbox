package com.izettle.tlv;

import static com.izettle.java.ValueChecks.*;
import com.izettle.java.Hex;

/**
 * ISO 7816, ASN.1 compliantish parser.
 * See http://www.gorferay.com/ber-tlv-length-fields/
 * See http://www.cardwerk.com/smartcards/smartcard_standard_ISO7816-4_annex-d.aspx
 * @author fidde
 */
public class TLVEncoder {

	private boolean strict;

	public TLVEncoder() {

	}

	public static void main(String[] args) {
		try {
			TLVEncoder encoder = new TLVEncoder();

			// 00
			System.out.println(Hex.toHexString(encoder.encodeLength(0)));
			// 7F
			System.out.println(Hex.toHexString(encoder.encodeLength(127)));
			// 8180
			System.out.println(Hex.toHexString(encoder.encodeLength(128)));
			// 81FF
			System.out.println(Hex.toHexString(encoder.encodeLength(255)));
			// 820100
			System.out.println(Hex.toHexString(encoder.encodeLength(256)));
			// 820101
			System.out.println(Hex.toHexString(encoder.encodeLength(257)));
			// 82FFFF
			System.out.println(Hex.toHexString(encoder.encodeLength(0xFFFF)));
			// 83010000
			System.out.println(Hex.toHexString(encoder.encodeLength(0xFFFF + 1)));
			// 83FFFFFF
			System.out.println(Hex.toHexString(encoder.encodeLength(0xFFFFFF)));
			// 8401000000
			System.out.println(Hex.toHexString(encoder.encodeLength(0xFFFFFF + 1)));
			// 8401000001
			System.out.println(Hex.toHexString(encoder.encodeLength(0xFFFFFF + 1 + 1)));

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public Object encode(byte[] tag, byte[] data) throws TLVException {
		validateTag(tag);
		return null;
	}

	byte[] encodeLength(int l) throws TLVException {

		if(l > 0xFFFFFFFFL) {
			throw new TLVException("Length exceeds integer 4 bytes length");
		}
		if(l < 0) {
			throw new TLVException("Length less than zero");
		}
		byte[] out;

		if (l > 0x00FFFFFF) {
			out = new byte[5];
			out[0] = (byte)0x84;
			out[1] = (byte)((l >> 24) & 0xff);
			out[2] = (byte)((l >> 16) & 0xff);
			out[3] = (byte)((l >> 8) & 0xff);
			out[4] = (byte)((l >> 0) & 0xff);
		} else if (l > 0x0000FFFF) {
			out = new byte[4];
			out[0] = (byte)0x83;
			out[1] = (byte)((l >> 16) & 0xff);
			out[2] = (byte)((l >> 8) & 0xff);
			out[3] = (byte)((l >> 0) & 0xff);
		} else if (l > 0x000000FF) {
			out = new byte[3];
			out[0] = (byte)0x82;
			out[1] = (byte)((l >> 8) & 0xff);
			out[2] = (byte)((l >> 0) & 0xff);
		} else if (l > 0x0000007F) {
			out = new byte[2];
			out[0] = (byte)0x81;
			out[1] = (byte)((l >> 0) & 0xff);
		} else {
			out = new byte[]{(byte)l};
		}

		return out;
	}

	private void validateTag(byte[] tag) throws TLVException {

		if(empty(tag)) {
			throw new TLVException("Malformed tag: empty");
		}

		/*
			 UNUSED: Leading byte, B8 + B7 is application class
			 UNUSED: Leading byte, B6 is primitive/constructed flag
		 */

		boolean isMultiByteTag = (tag[0] & 0x1f) == 0x1f;

		if(isMultiByteTag) {
			if(1 == tag.length) {
				throw new TLVException("Malformed tag: indicates multibyte, but is not");
			}
			if((tag[tag.length - 1] & 0x1f) == 0x1f) {
				throw new TLVException("Malformed tag: multibyte, but last byte doesn't close");
			}
		} else {
			if(1 != tag.length) {
				throw new TLVException("Malformed tag: indicates single byte, but is not");
			}
		}
	}

}
