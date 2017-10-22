package com.izettle.java;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class ValueChecks {

    private ValueChecks() {
    }

    /**
     * Checks if any of the arguments is null
     *
     * @param objects Object.
     * @return True if any object is null, false otherwise.
     */
    public static boolean anyNull(Object... objects) {
        if (objects == null) {
            return true;
        }
        for (Object object : objects) {
            if (object == null) {
                return true;
            }
        }
        return false;
    }

    public static boolean noneNull(Object... objects) {
        return !anyNull(objects);
    }

    public static boolean allNull(Object... objects) {
        if (objects == null) {
            return true;
        }
        for (Object object : objects) {
            if (object != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Utility method for shorter notation of possible null check before assignment,
     * eg:
     * {@code String s2 = s1 != null ? s1 : "";}
     * can instead be written as:
     * {@code String s2 = coalesce(s1, "");}
     * While adding type safety, this method is intended to behave in the exact way as MySQLs ifNull method
     *
     * @param <T> The type of the subject
     * @param <S> The type for the fallback value, must be same type, or subclass of T
     * @param subject check if this is null
     * @param fallback value to use if subject was null
     * @return the subject if not null, fallback otherwise
     */
    public static <T, S extends T> T coalesce(T subject, S fallback) {
        return subject != null ? subject : fallback;
    }

    /**
     * Checks if the given object is empty. Here empty is defined as: <ul> <li>a null {@link Object}</li> <li>an empty {@link String}</li>
     * <li>an empty {@link Array}</li> <li>an empty {@link Collection}</li> <li>an empty {@link Map}</li> <li>an empty {@link Optional}</li></ul>
     *
     * @param o
     * @return true if the object is empty, false otherwise
     */
    public static boolean empty(Object o) {
        if (o == null) {
            return true;
        }
        if (o instanceof CharSequence) {
            return onlyWhitespace((CharSequence) o);
        }
        if (o.getClass().isArray()) {
            return Array.getLength(o) == 0;
        }
        if (o instanceof Collection) {
            return ((Collection<?>) o).isEmpty();
        }
        if (o instanceof Map) {
            return ((Map<?, ?>) o).isEmpty();
        }
        if (o instanceof Optional) {
            return !((Optional) o).isPresent();
        }
        return false;
    }

    public static boolean onlyWhitespace(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            int codePoint = Character.codePointAt(cs, i);
            if (!Character.isWhitespace(codePoint) && !Character.isSpaceChar(codePoint)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if any of the objects satisfy {@code empty()}.
     *
     * @param objects the list of objects to check
     * @return True if any object satisfies {@code empty()}.
     */
    public static boolean anyEmpty(Object... objects) {
        if (empty(objects)) {
            return true;
        }
        for (Object object : objects) {
            if (empty(object)) {
                return true;
            }
        }
        return false;
    }

    public static boolean noneEmpty(Object... objects) {
        return !anyEmpty(objects);
    }

    public static boolean allEmpty(Object... objects) {
        if (empty(objects)) {
            return true;
        }
        for (Object object : objects) {
            if (!empty(object)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Utility method for shorter notation of check for emptiness before assignment, eg: String s3 = isEmpty(s1) ? s1 :s2;
     * can instead be written as: String s3 = ifEmpty(s1, s2); While adding type safety,
     *
     * @param <T> The type of the subject
     * @param <S> The type for the fallback value, must be same type, or subclass of T
     * @param subject check if this is null
     * @param fallback value to use if subject was null
     * @return the subject if not null, fallback otherwise
     */
    public static <T, S extends T> T ifEmpty(T subject, S fallback) {
        return empty(subject) ? fallback : subject;
    }

    /**
     * Assert a boolean expression, throwing {@code IllegalArgumentException}
     * if the test result is {@code false}.
     * <pre class="code">Assert.assertTrue(i &gt; 0, "The value must be greater than zero");</pre>
     * @param expression a boolean expression
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void assertTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Assert that an object is not {@code null} .
     * <pre class="code">Assert.assertNotNull(clazz, "The class must not be null");</pre>
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is {@code null}
     */
    public static <T> T assertNotNull(T object, String message) {
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
    public static String assertNotEmpty(String text, String message) {
        if (empty(text)) {
            throw new IllegalArgumentException(message);
        }
        return text;
    }

    /**
     * Assert that an array has elements; that is, it must not be
     * {@code null} and must have at least one element.
     * <pre class="code">Assert.assertNotEmpty(array, "The array must have elements");</pre>
     * @param array the array to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object array is {@code null} or has no elements
     */
    public static <T> T[] assertNotEmpty(T[] array, String message) {
        if (empty(array)) {
            throw new IllegalArgumentException(message);
        }
        return array;
    }

    /**
     * Assert that an object is not "empty". See {@link ValueChecks#empty(Object)}
     * for definition of "empty".
     * <pre class="code">Assert.assertNotEmpty(array, "The array must have elements");</pre>
     * <pre class="code">Assert.assertNotEmpty(optional, "The optional must exist");</pre>
     * <pre class="code">Assert.assertNotEmpty(object, "The object must not be null");</pre>
     * @param object the object to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object is "empty"
     */
    public static Object assertNotEmpty(Object object, String message) {
        if (empty(object)) {
            throw new IllegalArgumentException(message);
        }
        return object;
    }

    /**
     * Assert that an array is not null and has no null elements.
     * Note: Does not complain if the array is empty!
     * <pre class="code">Assert.assertNoNulls(array, "The array must have non-null elements");</pre>
     * @param array the array to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object array contains a {@code null} element
     */
    public static <T> T[] assertNoNulls(T[] array, String message) {
        if (anyNull(array)) {
            throw new IllegalArgumentException(message);
        }
        return array;
    }

    /**
     * Assert that an Collection is not null and has no null elements.
     * Note: Does not complain if the Collection is empty!
     * <pre class="code">Assert.assertNoNulls(array, "The Collection must have non-null elements");</pre>
     * @param collection the Collection to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the object Collection contains a {@code null} element
     */
    public static <T> Collection<T> assertNoNulls(Collection<T> collection, String message) {
        assertNotNull(collection, message);
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
     * <pre class="code">Assert.assertNotEmpty(collection, "Collection must have elements");</pre>
     * @param collection the collection to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the collection is {@code null} or has no elements
     */
    public static <T> Collection<T> assertNotEmpty(Collection<T> collection, String message) {
        if (empty(collection)) {
            throw new IllegalArgumentException(message);
        }
        return collection;
    }

    /**
     * Assert that a Map has entries; that is, it must not be {@code null}
     * and must have at least one entry.
     * <pre class="code">Assert.assertNotEmpty(map, "Map must have entries");</pre>
     * @param map the map to check
     * @param message the exception message to use if the assertion fails
     * @throws IllegalArgumentException if the map is {@code null} or has no entries
     */
    public static <V, K> Map<V, K> assertNotEmpty(Map<V, K> map, String message) {
        if (empty(map)) {
            throw new IllegalArgumentException(message);
        }
        return map;
    }

    /**
     * Assert a boolean expression, throwing {@code IllegalStateException}
     * if the test result is {@code false}. Call assertTrue if you wish to
     * throw IllegalArgumentException on an assertion failure.
     * <pre class="code">Assert.assertState(id == null, "The id property must not already be initialized");</pre>
     * @param expression a boolean expression
     * @param message the exception message to use if the assertion fails
     * @throws IllegalStateException if expression is {@code false}
     */
    public static void assertState(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

}
