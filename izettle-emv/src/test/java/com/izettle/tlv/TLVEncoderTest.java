package com.izettle.tlv;

import com.izettle.java.Hex;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by fidde on 16/12/14.
 */
public class TLVEncoderTest {

	@Test
	public void testLengthBounds() throws Exception {

		TLVEncoder encoder = new TLVEncoder();
		Assert.assertArrayEquals(encoder.encodeLength(0), Hex.hexToByteArray("00"));
		Assert.assertArrayEquals(encoder.encodeLength(127), Hex.hexToByteArray("7F"));
		Assert.assertArrayEquals(encoder.encodeLength(128), Hex.hexToByteArray("8180"));
		Assert.assertArrayEquals(encoder.encodeLength(255), Hex.hexToByteArray("81FF"));
		Assert.assertArrayEquals(encoder.encodeLength(256), Hex.hexToByteArray("820100"));
		Assert.assertArrayEquals(encoder.encodeLength(257), Hex.hexToByteArray("820101"));
		Assert.assertArrayEquals(encoder.encodeLength(0xFFFF), Hex.hexToByteArray("82FFFF"));
		Assert.assertArrayEquals(encoder.encodeLength(0xFFFF + 1), Hex.hexToByteArray("83010000"));
		Assert.assertArrayEquals(encoder.encodeLength(0xFFFFFF), Hex.hexToByteArray("83FFFFFF"));
		Assert.assertArrayEquals(encoder.encodeLength(0xFFFFFF + 1), Hex.hexToByteArray("8401000000"));
		Assert.assertArrayEquals(encoder.encodeLength(0xFFFFFF + 2), Hex.hexToByteArray("8401000001"));
	}

	@Test(expected = TLVException.class)
	public void testMalformedTag1() throws Exception {

		// Tag indicates multiple bytes in its lower register, but only has one tag.
		TLVEncoder encoder = new TLVEncoder();
		encoder.encode(new byte[]{(byte) 0x9f}, new byte[5]);
	}

	@Test(expected = TLVException.class)
	public void testMalformedTag2() throws Exception {

		// Multi-byte tag that isn't closed properly (lower 6 bits are set in last byte)
		TLVEncoder encoder = new TLVEncoder();
		encoder.encode(new byte[]{(byte) 0x0f, (byte) 0x1f}, new byte[5]);
	}

	@Test(expected = TLVException.class)
	public void testMalformedTag3() throws Exception {

		// 0x80 is not set in the middle byte(s) for a multi-byte tag.
		TLVEncoder encoder = new TLVEncoder();
		encoder.encode(new byte[]{(byte) 0x1f, (byte) 0x1f, (byte)0x20}, new byte[5]);
	}
}
