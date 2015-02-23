package com.izettle.tlv;

import com.izettle.java.Hex;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

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
    public void testMultiTags() throws Exception {
        byte[] tlvData = Hex.hexToByteArray("E003010203E103040506");
        TLVDecoder dec = new TLVDecoder();
        List<TLV> tlvs = dec.decode(tlvData);
        Assert.assertEquals(2, tlvs.size());
    }

    @Test
    public void testMultiTagsWithExpander() throws Exception {
        byte[] tlvData = Hex.hexToByteArray("E50AE003010203E103040506");
        TLVDecoder dec = new TLVDecoder();
        dec.addExpandTag(Hex.hexToByteArray("E5"));
        List<TLV> tlvs = dec.decode(tlvData);
        Assert.assertEquals(2, tlvs.size());
    }

    @Test(expected = TLVException.class)
    public void testMultiTagsWithExpanderWithWrongExpanderLength() throws Exception {
        byte[] tlvData = Hex.hexToByteArray("E50BE003010203E103040506");
        TLVDecoder dec = new TLVDecoder();
        dec.addExpandTag(Hex.hexToByteArray("E5"));
        dec.decode(tlvData);
    }

    @Test
    public void testExpandedTagWithMoreTagsAfter() throws Exception {
        TLVDecoder dec = new TLVDecoder();
        dec.addExpandTag(Hex.hexToByteArray("9A"));
        List<TLV> decodedTags = dec.decode(Hex.hexToByteArray("9A0E9F2602AABB9F2702CCDD5F200101"));
        Assert.assertEquals(3, decodedTags.size());
    }

    @Test(expected = TLVException.class)
    public void testMultiTagsWithGarbageEnd() throws Exception {
        byte[] tlvData = Hex.hexToByteArray("E003010203E10304050690");
        TLVDecoder dec = new TLVDecoder();
        List<TLV> tlvs = dec.decode(tlvData);
        Assert.assertEquals(2, tlvs.size());
    }

    @Test
    public void testNestedExpands() throws Exception {
        byte[] tlvData = Hex.hexToByteArray("E105E203E30101");
        TLVDecoder dec = new TLVDecoder();
        dec.addExpandTag(Hex.hexToByteArray("E1"));
        dec.addExpandTag(Hex.hexToByteArray("E2"));
        List<TLV> tlvs = dec.decode(tlvData);
        Assert.assertEquals(1, tlvs.size());
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

        byte[] tlvData = Hex.hexToByteArray("9F8101AA");
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
