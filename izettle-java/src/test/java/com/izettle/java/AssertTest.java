package com.izettle.java;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class AssertTest {

    @Test
    public void testIsTrue() {
        Assert.isTrue(true, "should be true");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsNotTrue() {
        Assert.isTrue(false, "should be true");
    }

    @Test
    public void testIsNull() {
        Assert.isNull(null, "Should be null");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsNullButAint() {
        Assert.isNull("stuff", "Should be null");
    }

    @Test
    public void testNotNull() {
        final String bananas = Assert.notNull("Bananas", "Bananas must not be null");
        org.junit.Assert.assertEquals(bananas, "Bananas");
    }

    @Test
    public void testNotNullEmptyString() {
        final String bananas = Assert.notNull("", "Bananas must not be null");
        org.junit.Assert.assertEquals(bananas, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotNullButIs() {
        Assert.notNull(null, "Bananas must not be null");
    }

    @Test
    public void testNotBlank() {
        final String bananas = Assert.notBlank("Bananas", "Bananas must not be blank");
        org.junit.Assert.assertEquals(bananas, "Bananas");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotBlankEmptyString() {
        Assert.notBlank("", "Bananas must not be blank");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotBlankWhiteSpace() {
        Assert.notBlank(" ", "Bananas must not be blank");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotBlankNull() {
        Assert.notBlank(null, "Bananas must not be blank");
    }

    @Test
    public void testNotEmptyForArrayMessage() {
        final String[] fruits = new String[]{"Banana", "Apple"};
        final String[] result = Assert.notEmpty(fruits, "Fruitbasket must not be empty");
        org.junit.Assert.assertArrayEquals(fruits, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmptyForArrayMessageEmpty() {
        final String[] fruits = null;
        Assert.notEmpty(fruits, "Fruitbasket must not be empty");
    }

    @Test
    public void testNoNullElements() {
        final String[] fruits = new String[]{"Banana", "Apple"};
        final String[] result = Assert.noNullElements(fruits, "Fruitbasket must not contain null elements");
        org.junit.Assert.assertArrayEquals(fruits, result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoNullElementsWithNullElements() {
        final String[] fruits = new String[]{"Banana", "Apple", null};
        Assert.noNullElements(fruits, "Fruitbasket must not contain null elements");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoNullElementsInCollectionWithNullElements() {
        final List<String> fruits = Arrays.asList("Banana", "Apple", null);
        Assert.noNullElements(fruits, "Fruitbasket must not contain null elements");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoNullElementsInCollectionIsNull() {
        final List<String> fruits = null;
        Assert.noNullElements(fruits, "Fruitbasket must not be null and not contain null elements");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmptyCollectionWithEmptyCollection() {
        final List<String> fruits = Collections.emptyList();
        Assert.notEmpty(fruits, "Fruitbasket must not be empty");
    }

    @Test
    public void testNoNullElementsInCollectionWithEmptyCollection() {
        final List<String> fruits = Collections.emptyList();
        final Collection<String> result = Assert.noNullElements(fruits, "Fruitbasket must not contain null elements");
        org.junit.Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testNotEmptyForCollectionMessage() {
        final List<String> fruits = Arrays.asList("Banana", "Apple", null);
        final Collection<String> result = Assert.notEmpty(fruits, "Fruitbasket must not be empty");
        org.junit.Assert.assertArrayEquals(fruits.toArray(), result.toArray());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotEmptyForMapMessageEmpty() {
        Map<String, String> fruits = new HashMap<String, String>();
        Assert.notEmpty(fruits, "Fruitbasket must not be empty");
    }

    @Test
    public void testNotEmptyForMapMessage() {
        Map<String, String> fruits = new HashMap<String, String>();
        fruits.put("Banana", "10");
        fruits.put("Apple", "5");
        final Map<String, String> result = Assert.notEmpty(fruits, "Fruitbasket must not be empty");
        org.junit.Assert.assertEquals(fruits, result);
    }

    @Test(expected = IllegalStateException.class)
    public void testIllegalState() {
        Assert.state(true == false, "Invalid state!!");
    }

    public void testValidState() {
        Assert.state(true == true, "Invalid state!!");
    }

}
