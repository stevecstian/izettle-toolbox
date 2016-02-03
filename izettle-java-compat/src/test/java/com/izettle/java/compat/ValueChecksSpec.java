package com.izettle.java.compat;

import com.izettle.java.compat.ValueChecks;

import static com.izettle.java.compat.ValueChecks.anyEmpty;
import static com.izettle.java.compat.ValueChecks.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class ValueChecksSpec {

    @Test
    public void allDefined() {
        assertFalse(ValueChecks.anyNull("Foo", 1));
        assertTrue(ValueChecks.anyNull("Foo", null));
        assertTrue(ValueChecks.anyNull(null, null));

        // If the args-array itself is null, it should return false.
        Object[] noObjects = null;
        assertTrue(ValueChecks.anyNull(noObjects));
        assertFalse(ValueChecks.anyNull("Foo", 2));
    }

    @Test
    public void noneNull() {
        assertTrue( ValueChecks.noneNull("hej", "svejs", "hallo"));
        assertTrue( ValueChecks.noneNull("", "", ""));
        assertFalse( ValueChecks.noneNull("hej", null, "hallo"));
        assertFalse( ValueChecks.noneNull(null, null, null));
    }

    @Test
    public void noneEmpty() {
        assertTrue( ValueChecks.noneEmpty("hej", "svejs", "hallo"));
        assertFalse( ValueChecks.noneEmpty("5", "BANANER!", ""));
        assertFalse( ValueChecks.noneEmpty(null, null, "hallo"));
        assertFalse( ValueChecks.noneEmpty("", "", null));

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

    @Test
    public void testIsTrue() {
        ValueChecks.assertTrue(true, "should be true");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsNotTrue() {
        ValueChecks.assertTrue(false, "should be true");
    }

    @Test
    public void testNotNull() {
        final String bananas = ValueChecks.assertNotNull("Bananas", "Bananas must not be null");
        org.junit.Assert.assertEquals(bananas, "Bananas");
    }

    @Test
    public void testNotNullEmptyString() {
        final String bananas = ValueChecks.assertNotNull("", "Bananas must not be null");
        org.junit.Assert.assertEquals(bananas, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotNullButIs() {
        ValueChecks.assertNotNull(null, "Bananas must not be null");
    }

    @Test
    public void testNotEmpty() {
        final String bananas = ValueChecks.assertNotEmpty("Bananas", "Bananas must not be Empty");
        org.junit.Assert.assertEquals(bananas, "Bananas");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmptyEmptyString() {
        ValueChecks.assertNotEmpty("", "Bananas must not be Empty");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmptyWhiteSpace() {
        ValueChecks.assertNotEmpty(" ", "Bananas must not be Empty");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmptyNull() {
        final String nullBanana = null;
        ValueChecks.assertNotEmpty(nullBanana, "Bananas must not be Empty");
    }

    @Test
    public void testNotEmptyForArrayMessage() {
        final String[] fruits = new String[]{"Banana", "Apple"};
        final String[] result = ValueChecks.assertNotEmpty(fruits, "Fruitbasket must not be empty");
        org.junit.Assert.assertArrayEquals(fruits, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmptyForArrayMessageEmpty() {
        final String[] fruits = null;
        ValueChecks.assertNotEmpty(fruits, "Fruitbasket must not be empty");
    }

    @Test
    public void testNoNullElements() {
        final String[] fruits = new String[]{"Banana", "Apple"};
        final String[] result = ValueChecks.assertNoNulls(fruits, "Fruitbasket must not contain null elements");
        org.junit.Assert.assertArrayEquals(fruits, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoNullElementsWithNullElements() {
        final String[] fruits = new String[]{"Banana", "Apple", null};
        ValueChecks.assertNoNulls(fruits, "Fruitbasket must not contain null elements");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoNullElementsInCollectionWithNullElements() {
        final List<String> fruits = Arrays.asList("Banana", "Apple", null);
        ValueChecks.assertNoNulls(fruits, "Fruitbasket must not contain null elements");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoNullElementsInCollectionIsNull() {
        final List<String> fruits = null;
        ValueChecks.assertNoNulls(fruits, "Fruitbasket must not be null and not contain null elements");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmptyCollectionWithEmptyCollection() {
        final List<String> fruits = Collections.emptyList();
        ValueChecks.assertNotEmpty(fruits, "Fruitbasket must not be empty");
    }

    @Test
    public void testNoNullElementsInCollectionWithEmptyCollection() {
        final List<String> fruits = Collections.emptyList();
        final Collection<String>
            result = ValueChecks.assertNoNulls(fruits, "Fruitbasket must not contain null elements");
        org.junit.Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testNotEmptyForCollectionMessage() {
        final List<String> fruits = Arrays.asList("Banana", "Apple", null);
        final Collection<String> result = ValueChecks.assertNotEmpty(fruits, "Fruitbasket must not be empty");
        org.junit.Assert.assertArrayEquals(fruits.toArray(), result.toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmptyForMapMessageEmpty() {
        Map<String, String> fruits = new HashMap<String, String>();
        ValueChecks.assertNotEmpty(fruits, "Fruitbasket must not be empty");
    }

    @Test
    public void testNotEmptyForMapMessage() {
        Map<String, String> fruits = new HashMap<String, String>();
        fruits.put("Banana", "10");
        fruits.put("Apple", "5");
        final Map<String, String> result = ValueChecks.assertNotEmpty(fruits, "Fruitbasket must not be empty");
        org.junit.Assert.assertEquals(fruits, result);
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalState() {
        ValueChecks.assertState(true == false, "Invalid assertState!!");
    }

    public void testValidState() {
        ValueChecks.assertState(true == true, "Invalid assertState!!");
    }
}
