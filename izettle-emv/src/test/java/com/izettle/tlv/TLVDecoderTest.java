package com.izettle.tlv;

import com.izettle.java.Hex;
import org.junit.Assert;
import org.junit.Test;
import java.util.List;

/**
 * Created by fidde on 17/12/14.
 */
public class TLVDecoderTest {

	@Test
	public void testSimpleDecode() throws Exception {

		TLVEncoder enc = new TLVEncoder();
		TLVDecoder dec = new TLVDecoder();

		TLV a = enc.encode(Hex.hexToByteArray("9F21"), new byte[16]);

		List<TLV> d = dec.decode(a.toBytes());
		Assert.assertTrue(1 == d.size());

		Assert.assertEquals("9F21", Hex.toHexString(d.get(0).getTag()));
		Assert.assertEquals(16, d.get(0).getValue().length);
		Assert.assertEquals("10", Hex.toHexString(d.get(0).getLength()));
	}

	@Test
	public void test5ByteLength() throws Exception {

		byte[] tlvData = Hex.hexToByteArray("4F8400000005AABBCCDDEE");
		TLVDecoder dec = new TLVDecoder();
		TLV tlv = dec.decode(tlvData).get(0);

		Assert.assertTrue(5 == tlv.getValue().length);
		Assert.assertEquals("AABBCCDDEE", Hex.toHexString(tlv.getValue()));
		Assert.assertEquals("8400000005", Hex.toHexString(tlv.getLength()));
	}

	@Test
	public void testMultiTag3ByteLength() throws Exception {

		byte[] tlvData = Hex.hexToByteArray("9F4F820002FFDD");
		TLVDecoder dec = new TLVDecoder();
		TLV tlv = dec.decode(tlvData).get(0);

		Assert.assertEquals("9F4F", Hex.toHexString(tlv.getTag()));
		Assert.assertEquals("FFDD", Hex.toHexString(tlv.getValue()));
		Assert.assertEquals("820002", Hex.toHexString(tlv.getLength()));
	}

	@Test(expected = TLVException.class)
	public void testInvalidTag() throws Exception {

		byte[] tlvData = Hex.hexToByteArray("9F1F01AA");
		TLVDecoder dec = new TLVDecoder();
		dec.decode(tlvData).get(0);
	}

	@Test
	public void testExpandedTag() throws Exception {

		TLVEncoder enc = new TLVEncoder();
		TLVDecoder dec = new TLVDecoder();

		TLV innerObject = dec.decode(Hex.hexToByteArray("9F27820002AABB")).get(0);
		TLV outerObject = enc.encode(Hex.hexToByteArray("9A"), innerObject.toBytes());

		dec.addExpandTag(Hex.hexToByteArray("9A"));

		List<TLV> decodedTags = dec.decode(outerObject.toBytes());
		Assert.assertTrue(1 == decodedTags.size());
		Assert.assertEquals("9F27", Hex.toHexString(decodedTags.get(0).getTag()));
	}

}
