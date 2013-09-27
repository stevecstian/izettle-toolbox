package com.izettle.java;

import static com.izettle.java.ValueChecks.isEmpty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Test;

public class ValueChecksSpec {

	@Test
	public void isDefined() {
		Integer i = null;
		assertFalse(ValueChecks.isDefined(i));
		i = 1;
		assertTrue(ValueChecks.isDefined(i));
	}

	@Test
	public void areDefined() {
		assertTrue(ValueChecks.areDefined("Foo", new Integer(1)));
		assertFalse(ValueChecks.areDefined("Foo", null));
		assertFalse(ValueChecks.areDefined(null, null));

		// If the args-array itself is null, it should return false.
		Object[] noObjects = null;
		assertFalse(ValueChecks.areDefined(noObjects));
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
}
