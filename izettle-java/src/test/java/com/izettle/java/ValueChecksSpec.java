package com.izettle.java;

import static com.izettle.java.ValueChecks.anyEmpty;
import static com.izettle.java.ValueChecks.empty;
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
	public void allDefined() {
		assertFalse(ValueChecks.anyNull("Foo", new Integer(1)));
		assertTrue(ValueChecks.anyNull("Foo", null));
		assertTrue(ValueChecks.anyNull(null, null));

		// If the args-array itself is null, it should return false.
		Object[] noObjects = null;
		assertTrue(ValueChecks.anyNull(noObjects));
		assertFalse(ValueChecks.anyNull("Foo", new Integer(2)));
	}

	@Test
	public void isEmpty_emptyValues() {
		assertTrue("null", empty(null));
		assertTrue("single space", empty(" "));
		assertTrue("double whitespace", empty(" \n"));
		assertTrue("different types of whitespace", empty("\t\f \r\n"));
		assertTrue("unicode whitespace", empty(" \u2000 \u2001 \u2002 \u2003 \u2004 \u2005 \u2006 \u2007 \u2008 \u2009 \u200a \u202f \u205f \u3000 "));
		assertTrue("empty string", empty(""));
		assertTrue("empty array", empty(new Object[0]));
		assertTrue("empty array", empty(new byte[0]));
		assertTrue("empty array list", empty(new ArrayList<String>()));
		assertTrue("empty map", empty(new HashMap<String, String>()));
	}

	@Test
	public void isEmpty_nonEmptyValues() {
		assertFalse("single character", empty("a"));
		assertFalse("funky utf8 character (snowman)", empty("☃"));
		assertFalse("one element array", empty(new Object[1]));
		assertFalse("single element list", empty(Arrays.asList("foo")));
		assertFalse("single key map", empty(Collections.singletonMap("foo", "bar")));
	}

	@Test
	public void anyEmpty_emptyValues() {
		assertTrue(anyEmpty((Object[]) null));
		assertTrue(anyEmpty(""));
		assertTrue(anyEmpty(99, ""));
		assertTrue(anyEmpty(99, null));
		assertTrue(anyEmpty(null, "test"));
		assertTrue(anyEmpty(new Object(), new HashMap<String, String>()));
	}

	@Test
	public void allNullEmpty() {
		assertTrue(ValueChecks.allNull(null, null));
		assertFalse(ValueChecks.allNull("A", null));
		assertFalse(ValueChecks.allNull(null, "A"));
		assertTrue(ValueChecks.allNull((Object[]) null));
		assertTrue(ValueChecks.allNull((Object) null));
	}

	@Test
	public void allEmpty() {
		assertTrue(ValueChecks.allEmpty(null, null));
		assertFalse(ValueChecks.allEmpty("A", null));
		assertFalse(ValueChecks.allEmpty(null, "A"));
		assertTrue(ValueChecks.allEmpty("", null));
		assertTrue(ValueChecks.allEmpty(null, ""));
		assertTrue(ValueChecks.allEmpty((Object[]) null));
		assertTrue(ValueChecks.allEmpty((Object) null));
	}

	@Test
	public void anyEmpty_nonEmptyValues() {
		assertFalse(anyEmpty(new Object()));
		assertFalse(anyEmpty("a", 9));
		assertFalse(anyEmpty(99, 98));
	}

	@Test
	public void coalesce() {
		Object nullObject = null;
		assertEquals("Foo", ValueChecks.coalesce(nullObject, "Foo"));
		Integer one = null;
		Integer two = 2;
		assertEquals(two, ValueChecks.coalesce(one, two));
		one = 1;
		assertEquals(one, ValueChecks.coalesce(one, two));
		Assert.assertNull(ValueChecks.coalesce(null, null));
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
