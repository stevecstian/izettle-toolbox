package com.izettle.java;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

public class ValueChecks {

	/**
	 * An alternative to the semantics of null-checks.
	 *
	 * @param object Object.
	 * @return True if the object is defined (not null), false otherwise.
	 */
	public static boolean defined(Object object) {
		return object != null;
	}

	/**
	 * An alternative to the semantics of null-checks.
	 *
	 * @param objects Objects.
	 * @return True if all objects have defined values (not null), false otherwise.
	 */
	public static boolean allDefined(Object... objects) {
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
	 * Checks if the object are undefined (null).
	 *
	 * @param object Object.
	 * @return True if the object is undefined (null), false otherwise.
	 */
	public static boolean undefined(Object object) {
		return !defined(object);
	}

	/**
	 * Checks if the object are undefined (null).
	 *
	 * @param object Object.
	 * @return True if any object is undefined (null), false otherwise.
	 */
	public static boolean anyUndefined(Object... object) {
		return !allDefined(object);
	}

	/**
	 * Returns first non-null parameter.
	 *
	 * @param <T> The type of the subject
	 * @param o1 First object.
	 * @param o2 Second object.
	 * @return First non-null parameter, or null of no non-null parameter found.
	 */
	public static <T> T coalesce(T o1, T o2) {
		return o1 != null ? o1 : o2;
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
	 * The inverse of <code>isEmpty()</code>.
	 *
	 * @param o Object under test.
	 * @return True if the object is not empty, false otherwise.
	 */
	public static boolean notEmpty(Object o) {
		return !empty(o);
	}
}
