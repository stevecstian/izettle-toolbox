package com.izettle.java;

import static com.izettle.java.Hex.hexToByte;
import static com.izettle.java.Hex.hexToByteArray;
import static com.izettle.java.Hex.toHexString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class HexSpec {

	@Test
	public void testByteArrToHexString() {
		assertEquals("A5B2FFD0", toHexString(new byte[] { (byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0 }));
		assertEquals("00A0BBF0", toHexString(new byte[] { (byte) 0x00, (byte) 0xA0, (byte) 0xBB, (byte) 0xF0 }));
		assertEquals("FF00010203", toHexString(new byte[] { (byte) 0xFF, (byte) 0x00, (byte) 0x01, (byte) 0x2, (byte) 0x03 }));
		assertEquals("", toHexString(null));
	}

	@Test
	public void testByteToHex() {
		assertEquals("A5", toHexString((byte) 0xA5));
		assertEquals("00", toHexString((byte) 0x00));
	}

	@Test
	public void testHexStringToByteArr() {
		assertArrayEquals(new byte[] { (byte) 0xA5, (byte) 0xB2, (byte) 0xFF, (byte) 0xD0 }, hexToByteArray("A5B2FFD0"));
		assertArrayEquals(new byte[] { (byte) 0x00, (byte) 0xA0, (byte) 0xBB, (byte) 0xF0 }, hexToByteArray("00A0BBF0"));
		assertArrayEquals(new byte[] { (byte) 0xFF, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03 }, hexToByteArray("FF00010203"));
		assertNull(hexToByteArray(null));
	}

	@Test
	public void testHexToByte() {
		assertEquals((byte) 0x01, hexToByte("01"));
		assertEquals((byte) 0x77, hexToByte("77"));
		assertEquals((byte) 0x70, hexToByte("70"));
		assertEquals((byte) 0xA5, hexToByte("A5"));
		assertEquals((byte) 0xFF, hexToByte("FF"));
		assertEquals((byte) 0x00, hexToByte("00"));
	}
}
