package com.izettle.java;

/*
 * Copyright 2002-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.Collection;
import java.util.Map;

/**
 * Assertion utility class that assists in validating arguments.
 *
 * <p>Useful for identifying programmer errors early and clearly at runtime.
 *
 * <p>For example, if the contract of a public method states it does not
 * allow {@code null} arguments, {@code Assert} can be used to validate that
 * contract. Doing this clearly indicates a contract violation when it
 * occurs and protects the class's invariants.
 *
 * <p>Typically used to validate method arguments rather than configuration
 * properties, to check for cases that are usually programmer errors rather
 * than configuration errors. In contrast to configuration initialization
 * code, there is usually no point in falling back to defaults in such methods.
 *
 * <p>This class is similar to JUnit's assertion library. If an argument value is
 * deemed invalid, an {@link IllegalArgumentException} is thrown (typically).
 * For example:
 *
 * <pre class="code">
 * Assert.notNull(clazz, "The class must not be null");
 * Assert.isTrue(i > 0, "The value must be greater than zero");</pre>
 *
 */
public abstract class Assert {

    /**
     * Assert a boolean expression, throwing {@code IllegalArgumentException}
     * if the test result is {@code false}.
     * <pre class="code">Assert.isTrue(i &gt; 0, "The value must be greater than zero");</pre>
     * @param expression a boolean expression
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is {@code null} .
     * <pre class="code">Assert.isNull(value, "The value must be null");</pre>
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is not {@code null}
     */
    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is not {@code null} .
     * <pre class="code">Assert.notNull(clazz, "The class must not be null");</pre>
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static <T> T notNull(T object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
        return object;
    }

    /**
     * Assert that the given String has valid text content; that is, it must not
     * be {@code null} and must contain at least one non-whitespace character.
     * <pre class="code">Assert.hasText(name, "'name' must not be empty");</pre>
     * @param text the String to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the text does not contain valid text content
     */
    public static String notBlank(String text, String message) {
        if (ValueChecks.empty(text)) {
            throw new IllegalArgumentException(message);
        }
        return text;
    }

    /**
     * Assert that an array has elements; that is, it must not be
     * {@code null} and must have at least one element.
     * <pre class="code">Assert.notEmpty(array, "The array must have elements");</pre>
     * @param array the array to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object array is {@code null} or has no elements
     */
    public static <T> T[] notEmpty(T[] array, String message) {
        if (ValueChecks.empty(array)) {
            throw new IllegalArgumentException(message);
        }
        return array;
    }

    /**
     * Assert that an array is not null and has no null elements.
     * Note: Does not complain if the array is empty!
     * <pre class="code">Assert.noNullElements(array, "The array must have non-null elements");</pre>
     * @param array the array to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object array contains a {@code null} element
     */
    public static <T> T[] noNullElements(T[] array, String message) {
        notNull(array, message);
        for (Object element : array) {
            if (element == null) {
                throw new IllegalArgumentException(message);
            }
        }
        return array;
    }

    /**
     * Assert that an Collection is not null and has no null elements.
     * Note: Does not complain if the Collection is empty!
     * <pre class="code">Assert.noNullElements(array, "The Collection must have non-null elements");</pre>
     * @param collection the Collection to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object Collection contains a {@code null} element
     */
    public static <T> Collection<T> noNullElements(Collection<T> collection, String message) {
        notNull(collection, message);
        for (Object element : collection) {
            if (element == null) {
                throw new IllegalArgumentException(message);
            }
        }
        return collection;
    }

    /**
     * Assert that a collection has elements; that is, it must not be
     * {@code null} and must have at least one element.
     * <pre class="code">Assert.notEmpty(collection, "Collection must have elements");</pre>
     * @param collection the collection to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the collection is {@code null} or has no elements
     */
    public static <T> Collection<T> notEmpty(Collection<T> collection, String message) {
        if (ValueChecks.empty(collection)) {
            throw new IllegalArgumentException(message);
        }
        return collection;
    }

    /**
     * Assert that a Map has entries; that is, it must not be {@code null}
     * and must have at least one entry.
     * <pre class="code">Assert.notEmpty(map, "Map must have entries");</pre>
     * @param map the map to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the map is {@code null} or has no entries
     */
    public static <V, K> Map<V, K> notEmpty(Map<V, K> map, String message) {
        if (ValueChecks.empty(map)) {
            throw new IllegalArgumentException(message);
        }
        return map;
    }

    /**
     * Assert a boolean expression, throwing {@code IllegalStateException}
     * if the test result is {@code false}. Call isTrue if you wish to
     * throw IllegalArgumentException on an assertion failure.
     * <pre class="code">Assert.state(id == null, "The id property must not already be initialized");</pre>
     * @param expression a boolean expression
     * @param message the exception message to use if the assertion fails
     * @throws IllegalStateException if expression is {@code false}
     */
    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

}
