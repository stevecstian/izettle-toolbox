package com.izettle.tlv;

import org.junit.Test;

/**
 *
 * @author staffan
 */
public class TLVUtilsTest {

    public TLVUtilsTest() {
    }

    @Test(expected = TLVException.class)
    public void testValidateTagShouldFailForInvalid2Byte() throws Exception {
        TLVUtils.validateTag(new byte[]{(byte) 0x9F, (byte) 0x82});
    }

    @Test(expected = TLVException.class)
    public void testValidateTagShouldFailForInvalid3Byte() throws Exception {
        TLVUtils.validateTag(new byte[]{(byte) 0x9F, (byte) 0x12, (byte) 0x82});
    }

    @Test
    public void testValidateTagShouldValidate1ByteTag() throws Exception {
        TLVUtils.validateTag(new byte[]{(byte) 0x50});
    }

    @Test
    public void testValidateTagShouldValidate2ByteTag() throws Exception {
        TLVUtils.validateTag(new byte[]{(byte) 0x9F, (byte) 0x12});
    }

    @Test
    public void testValidateTagShouldValidate3ByteTag() throws Exception {
        TLVUtils.validateTag(new byte[]{(byte) 0xDF, (byte) 0xAE, (byte) 0x04});
    }
}
