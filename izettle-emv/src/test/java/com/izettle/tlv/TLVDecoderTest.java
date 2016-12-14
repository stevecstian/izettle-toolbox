package com.izettle.tlv;

import static org.junit.Assert.assertEquals;

import java.util.List;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TLVDecoderTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSimpleDecode() throws Exception {

        TLVEncoder enc = new TLVEncoder();
        TLVDecoder dec = new TLVDecoder();

        TLV a = enc.encode(Hex.hexToByteArray("9F21"), new byte[16]);

        List<TLV> d = dec.decode(a.toBytes());
        assertEquals(1, d.size());

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
        List<TLV> decodedTags = dec.decode(Hex.hexToByteArray("9A0A9F2602AABB9F2702AABB5F200101"));
        Assert.assertEquals(3, decodedTags.size());
    }

    @Test
    public void testMultiTagsWithGarbageEnd() throws Exception {
        byte[] tlvData = Hex.hexToByteArray("E003010203E10304050690");
        TLVDecoder dec = new TLVDecoder();
        thrown.expect(TLVException.class);
        thrown.expectMessage("Malformed data: Tag, but no length present");
        dec.decode(tlvData);
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

        assertEquals(5, tlv.getValue().length);
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

    @Test
    public void testInvalidTag() throws Exception {
        byte[] tlvData = Hex.hexToByteArray("9F8101AA");
        TLVDecoder dec = new TLVDecoder();
        thrown.expect(TLVException.class);
        thrown.expectMessage("Malformed length, first length byte indiciates length that doesn't fit");
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
        assertEquals(1, decodedTags.size());
        Assert.assertEquals("9F27", Hex.toHexString(decodedTags.get(0).getTag()));
    }

    @Test
    public void testStrictModeGoodData() throws Exception {
        TLVDecoder dec = new TLVDecoder();
        dec.setStrictMode(true);
        List<TLV> decodedTags = dec.decode(Hex.hexToByteArray("DF0301FEDF0401FF"));
        Assert.assertEquals(2, decodedTags.size());
    }

    @Test
    public void testStrictModeLeftPadded() throws Exception {
        TLVDecoder dec = new TLVDecoder();
        dec.setStrictMode(true);
        thrown.expect(TLVException.class);
        thrown.expectMessage("Malformed length, first length byte indiciates length that doesn't fit");
        // Uneven number of header zeroes = not valid
        dec.decode(Hex.hexToByteArray("0000000000DF0301FEDF0401FF"));
    }

    @Test
    public void testStrictModeRightPadded() throws Exception {
        TLVDecoder dec = new TLVDecoder();
        dec.setStrictMode(true);
        // Even number of trailing zeroes is valid.
        List<TLV> decodedTags = dec.decode(Hex.hexToByteArray("DF0301FEDF0401FF00000000"));
        int numberOfZeroTags = 0;
        for (TLV tlv : decodedTags) {
            if ("00".equals(Hex.toHexString(tlv.getTag()))) {
                numberOfZeroTags++;
            }
        }
        Assert.assertEquals(2, numberOfZeroTags);
    }

    @Test
    public void testNonStrictMode() throws Exception {
        TLVDecoder dec = new TLVDecoder();
        List<TLV> decodedTags;
        // Uneven number of trailing zeroes is valid
        decodedTags = dec.decode(Hex.hexToByteArray("DF0301FEDF0401FF0000000000"));
        Assert.assertEquals(2, decodedTags.size());
        // Uneven number of heading zeroes is valid
        decodedTags = dec.decode(Hex.hexToByteArray("000000DF0301FEDF0401FF"));
        Assert.assertEquals(2, decodedTags.size());
        // Padding before, between, after.
        decodedTags = dec.decode(Hex.hexToByteArray("000000DF0301FE0000000000DF0401FF000000"));
        Assert.assertEquals(2, decodedTags.size());
    }
}
