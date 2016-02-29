package com.izettle.tlv;

import static com.izettle.tlv.TLVEncoder.encodeLength;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TLVEncoderTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testLengthBounds() throws Exception {
        Assert.assertArrayEquals(encodeLength(0), Hex.hexToByteArray("00"));
        Assert.assertArrayEquals(encodeLength(127), Hex.hexToByteArray("7F"));
        Assert.assertArrayEquals(encodeLength(128), Hex.hexToByteArray("8180"));
        Assert.assertArrayEquals(encodeLength(255), Hex.hexToByteArray("81FF"));
        Assert.assertArrayEquals(encodeLength(256), Hex.hexToByteArray("820100"));
        Assert.assertArrayEquals(encodeLength(257), Hex.hexToByteArray("820101"));
        Assert.assertArrayEquals(encodeLength(0xFFFF), Hex.hexToByteArray("82FFFF"));
        Assert.assertArrayEquals(encodeLength(0xFFFF + 1), Hex.hexToByteArray("83010000"));
        Assert.assertArrayEquals(encodeLength(0xFFFFFF), Hex.hexToByteArray("83FFFFFF"));
        Assert.assertArrayEquals(encodeLength(0xFFFFFF + 1), Hex.hexToByteArray("8401000000"));
        Assert.assertArrayEquals(encodeLength(0xFFFFFF + 2), Hex.hexToByteArray("8401000001"));
    }

    @Test
    public void testMalformedTag1() throws Exception {
        // Tag indicates multiple bytes in its lower register, but only has one tag.
        thrown.expect(TLVException.class);
        thrown.expectMessage("Malformed tag: indicates multibyte, but is not");
        new TLVEncoder().encode(new byte[]{(byte) 0x9f}, new byte[5]);
    }

    @Test
    public void testMalformedTag2() throws Exception {
        // Multi-byte tag that isn't closed properly (lower 6 bits are set in last byte)
        thrown.expect(TLVException.class);
        thrown.expectMessage("Malformed tag: indicates single byte, but is not");
        new TLVEncoder().encode(new byte[]{(byte) 0x0f, (byte) 0x1f}, new byte[5]);
    }

    @Test
    public void testMalformedTag3() throws Exception {
        // 0x80 is not set in the middle byte(s) for a multi-byte tag.
        thrown.expect(TLVException.class);
        thrown.expectMessage("Malformed tag: multibyte, but 0x80 not set in tag byte 1");
        new TLVEncoder().encode(new byte[]{(byte) 0x1f, (byte) 0x1f, (byte) 0x20}, new byte[5]);
    }

    @Test
    public void testNullValue() throws Exception {
        thrown.expect(TLVException.class);
        thrown.expectMessage("Malformed tag: multibyte, but 0x80 not set in tag byte 1");
        new TLVEncoder().encode(new byte[]{(byte) 0x1f, (byte) 0x1f, (byte) 0x20}, null);
    }
}
