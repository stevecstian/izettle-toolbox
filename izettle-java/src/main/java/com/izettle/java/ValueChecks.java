package com.izettle.java;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

public class ValueChecks {

	/**
	 * An alternative to the semantics of null-checks.
	 *
	 * @param object Object.
	 * @return True if the object has a defined value (not null), false otherwise.
	 */
	public static boolean isDefined(Object object) {
		return object != null;
	}

	/**
	 * An alternative to the semantics of null-checks.
	 *
	 * @param objects Objects.
	 * @return True if all objects have defined values (not null), false otherwise.
	 */
	public static boolean areDefined(Object... objects) {
		if (objects != null) {
			for (Object object : objects) {
				if (object == null) {
					return false;
				}
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * The inverse of <code>areDefined</code>.
	 *
	 * @param object Object.
	 * @return True any of the specified objects is null, false otherwise.
	 */
	public static boolean notDefined(Object... object) {
		return !areDefined(object);
	}

	/**
	 * Utility method for shorter notation of possible null check before assignment, eg: String s2 = s1 != null ? s1 :
	 * ""; can instead be written as: String s2 = define(s1, "");
	 *
	 * @param <T> The type of the subject
	 * @param <S> The type for the fallback value, must be same type, or subclass of T
	 * @param subject check if this is null
	 * @param fallback value to use if subject was null
	 * @return the subject if not null, fallback otherwise
	 */
	public static <T, S extends T> T define(T subject, S fallback) {
		if (isDefined(subject)) {
			return subject;
		}
		return fallback;
	}

	/**
	 * Checks if the given object is empty. Here empty is defined as: <ul> <li>a null {@link Object}</li> <li>an empty {@link String}</li>
	 * <li>an empty {@link java.lang.reflect.Array}</li> <li>an empty {@link java.util.Collection}</li> <li>an empty {@link java.util.Map}</li> </ul>
	 *
	 * @param o
	 * @return true if the object is empty, false otherwise
	 */
	public static boolean isEmpty(Object o) {
		if (o == null) {
			return true;
		}
		if (o instanceof String) {
			return ((String) o).length() == 0;
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
		return false;
	}

	/**
	 * The inverse of <code>isEmpty()</code>.
	 *
	 * @param o Object under test.
	 * @return True if the object is not empty, false otherwise.
	 */
	public static boolean notEmpty(Object o) {
		return !isEmpty(o);
	}
}
