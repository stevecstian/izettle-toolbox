package com.izettle.java;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

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
	 * <code>String s2 = s1 != null ? s1 : "";</code>
	 * can instead be written as:
	 * <code>String s2 = coalesce(s1, "");</code>
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
	 * <li>an empty {@link java.lang.reflect.Array}</li> <li>an empty {@link java.util.Collection}</li> <li>an empty {@link java.util.Map}</li> </ul>
	 *
	 * @param o
	 * @return true if the object is empty, false otherwise
	 */
	public static boolean empty(Object o) {
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
	 * Checks if any of the objects satisfy <code>empty()</code>.
	 *
	 * @param objects the list of objects to check
	 * @return True if any object satisfies <code>empty()</code>.
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
}
