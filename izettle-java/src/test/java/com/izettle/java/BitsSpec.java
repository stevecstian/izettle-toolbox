package com.izettle.java;

import static com.izettle.java.Bits.intTo4BytesBigEndian;
import static com.izettle.java.Bits.toBitString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class BitsSpec {

	@Test
	public void testByteToBitString() {
		assertEquals("00000000", toBitString((byte) 0x00));
		assertEquals("00001010", toBitString((byte) 0x0A));
		assertEquals("00001111", toBitString((byte) 0x0F));
		assertEquals("11111010", toBitString((byte) 0xFA));
		assertEquals("11111111", toBitString((byte) 0xFF));
	}

	@Test
	public void testByteArrayToBitString() {
		assertEquals("00000000", toBitString(new byte[] { (byte) 0x00 }));
		assertEquals("00000001", toBitString(new byte[] { (byte) 0x01 }));
		assertEquals("00001010", toBitString(new byte[] { (byte) 0x0A }));
		assertEquals("10101001", toBitString(new byte[] { (byte) 0xA9 }));
		assertEquals("11111111", toBitString(new byte[] { (byte) 0xFF }));
		assertEquals("0000000011111111", toBitString(new byte[] { (byte) 0x00, (byte) 0xFF }));
		assertEquals("1010100111110001", toBitString(new byte[] { (byte) 0xA9, (byte) 0xF1 }));
	}

	@Test
	public void testIntToByteArr() {
		assertArrayEquals(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 }, intTo4BytesBigEndian(0));
		assertArrayEquals(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF }, intTo4BytesBigEndian(255));
		assertArrayEquals(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00 }, intTo4BytesBigEndian(256));
		assertArrayEquals(new byte[] { (byte) 0x00, (byte) 0x00, (byte) 0x04, (byte) 0x00 }, intTo4BytesBigEndian(1024));
	}
}
