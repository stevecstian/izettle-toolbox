package com.izettle.java;

import static com.izettle.java.ValueChecks.anyEmpty;
import static com.izettle.java.ValueChecks.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Assert;
import org.junit.Test;

public class ValueChecksSpec {

	@Test
	public void isNull() {
		Integer i = null;
		assertTrue(ValueChecks.isNull(i));
		i = 1;
		assertFalse(ValueChecks.isNull(i));
		assertTrue(ValueChecks.isNull(null));
		assertFalse(ValueChecks.isNull("Foo"));
	}

	@Test
	public void allDefined() {
		assertFalse(ValueChecks.anyNull("Foo", new Integer(1)));
		assertTrue(ValueChecks.anyNull("Foo", null));
		assertTrue(ValueChecks.anyNull(null, null));

		// If the args-array itself is null, it should return false.
		Object[] noObjects = null;
		assertTrue(ValueChecks.isNull(noObjects));
	}

	@Test
	public void anyNull() {
		assertFalse(ValueChecks.anyNull("Foo", new Integer(2)));
	}

	@Test
	public void isEmpty_emptyValues() {
		assertTrue("null", isEmpty(null));
		assertTrue("empty string", isEmpty(""));
		assertTrue("empty array", isEmpty(new Object[0]));
		assertTrue("empty array", isEmpty(new byte[0]));
		assertTrue("empty array list", isEmpty(new ArrayList<String>()));
		assertTrue("empty map", isEmpty(new HashMap<String, String>()));
	}

	@Test
	public void isEmpty_nonEmptyValues() {
		assertFalse("single space", isEmpty(" "));
		assertFalse("one element array", isEmpty(new Object[1]));
		assertFalse("single element list", isEmpty(Arrays.asList("foo")));
		assertFalse("single key map", isEmpty(Collections.singletonMap("foo", "bar")));
	}

	@Test
	public void coalesce() {
		Object nullObject = null;
		assertEquals("Foo", ValueChecks.coalesce(nullObject, "Foo"));
	}

	@Test
	public void anyEmpty_emptyValues() {
		assertTrue(anyEmpty(null));
		assertTrue(anyEmpty(""));
		assertTrue(anyEmpty(99, ""));
		assertTrue(anyEmpty(99, null));
		assertTrue(anyEmpty(null, "test"));
		assertTrue(anyEmpty(new Object(), new HashMap<String, String>()));
	}

	@Test
	public void anyEmpty_nonEmptyValues() {
		assertFalse(anyEmpty(new Object()));
		assertFalse(anyEmpty(" ", 9));
		assertFalse(anyEmpty(99, 98));
	}

	@Test
	public void ifNull() {
		Integer one = null;
		Integer two = 2;
		assertEquals(two, ValueChecks.ifNull(one, two));
		one = 1;
		assertEquals(one, ValueChecks.ifNull(one, two));
		Assert.assertNull(ValueChecks.ifNull(null, null));
	}

	@Test
	public void ifEmpty() {
		String one = "";
		String two = "two";
		assertEquals(two, ValueChecks.ifEmpty(one, two));
		one = "one";
		assertEquals(one, ValueChecks.ifEmpty(one, two));
		Assert.assertNull(ValueChecks.ifEmpty("", null));
	}
}
