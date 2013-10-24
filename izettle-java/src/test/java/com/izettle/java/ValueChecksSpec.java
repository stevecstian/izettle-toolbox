package com.izettle.java;

import static com.izettle.java.ValueChecks.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Test;

public class ValueChecksSpec {

	@Test
	public void defined() {
		Integer i = null;
		assertFalse(ValueChecks.defined(i));
		i = 1;
		assertTrue(ValueChecks.defined(i));
	}

	@Test
	public void allDefined() {
		assertTrue(ValueChecks.allDefined("Foo", new Integer(1)));
		assertFalse(ValueChecks.allDefined("Foo", null));
		assertFalse(ValueChecks.allDefined(null, null));

		// If the args-array itself is null, it should return false.
		Object[] noObjects = null;
		assertFalse(ValueChecks.defined(noObjects));
	}

	@Test
	public void undefined() {
		assertTrue(ValueChecks.undefined(null));
		assertFalse(ValueChecks.undefined("Foo"));
	}

	@Test
	public void anyUndefined() {
		assertFalse(ValueChecks.anyUndefined("Foo", new Integer(2)));
	}

	@Test
	public void isEmpty_emptyValues() {
		assertTrue("null", empty(null));
		assertTrue("empty string", empty(""));
		assertTrue("empty array", empty(new Object[0]));
		assertTrue("empty array", empty(new byte[0]));
		assertTrue("empty array list", empty(new ArrayList<String>()));
		assertTrue("empty map", empty(new HashMap<String, String>()));
	}

	@Test
	public void isEmpty_nonEmptyValues() {
		assertFalse("single space", empty(" "));
		assertFalse("one element array", empty(new Object[1]));
		assertFalse("single element list", empty(Arrays.asList("foo")));
		assertFalse("single key map", empty(Collections.singletonMap("foo", "bar")));
	}

	@Test
	public void coalesce() {
		Object nullObject = null;
		assertEquals("Foo", ValueChecks.coalesce(nullObject, "Foo"));
	}
}
