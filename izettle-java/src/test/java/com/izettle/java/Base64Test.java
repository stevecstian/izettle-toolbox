package com.izettle.java;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class Base64Test {

	@Test
	public void testByteArrToB64String() throws Exception {
		assertEquals("dGVzdGluZyBCYXNl", Base64.byteArrToB64String("testing Base".getBytes()));
		assertEquals("dGVzdGluZyBCYXNlNjQ", Base64.byteArrToB64String("testing Base64".getBytes()));
		assertEquals("YW5vdGhlciB0ZXN0IFteKsOWw6TDtg", Base64.byteArrToB64String("another test [^*Öäö".getBytes()));
		assertEquals("I8KkJSYvKA", Base64.byteArrToB64String("#¤%&/(".getBytes()));
	}

	@Test
	public void testB64StringToByteArr() {
		assertEquals("testing Base", new String(Base64.b64StringToByteArr("dGVzdGluZyBCYXNl")));
		assertEquals("testing Base64", new String(Base64.b64StringToByteArr("dGVzdGluZyBCYXNlNjQ")));
		assertEquals("another test [^*Öäö", new String(Base64.b64StringToByteArr("YW5vdGhlciB0ZXN0IFteKsOWw6TDtg")));
		assertEquals("#¤%&/(", new String(Base64.b64StringToByteArr("I8KkJSYvKA")));
	}

	@Test
	public void testByteArrToB64StringEmptyNullCases() throws Exception {
		assertNull(Base64.byteArrToB64String(null));
		assertEquals("", Base64.byteArrToB64String(new byte[0]));
	}

	@Test
	public void testB64StringToByteArrEmptyNullCases() {
		assertEquals(null, Base64.b64StringToByteArr(null));
		assertArrayEquals(new byte[0], Base64.b64StringToByteArr(""));
	}
}
