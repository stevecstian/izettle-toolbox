package com.izettle.tlv;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TLVUtilsTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testValidateTagShouldFailForInvalid2Byte() throws Exception {
        thrown.expect(TLVException.class);
        thrown.expectMessage("Malformed tag: multibyte, but last byte doesn't close");
        TLVUtils.validateTag(new byte[]{(byte) 0x9F, (byte) 0x82});
    }

    @Test
    public void testValidateTagShouldFailForInvalid3Byte() throws Exception {
        thrown.expect(TLVException.class);
        thrown.expectMessage("Malformed tag: multibyte, but last byte doesn't close");
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
