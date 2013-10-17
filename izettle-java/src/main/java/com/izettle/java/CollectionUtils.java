package com.izettle.java;

import static com.izettle.java.ValueChecks.isEmpty;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author adam
 */
public class CollectionUtils {

	private CollectionUtils() {
	}

	/**
	 * Will partition a collection to a list of collections, each with maximum size of partitionSize. The
	 * partitioning will be made according to the order of the argument, and each of the returned partitions will be
	 * either a List, a Set or a SortedSet, depending on the provided argument.
	 * @param <T>
	 * @param largeCollection
	 * @param partitionSize
	 * @return
	 */
	public static <T> List<Collection<T>> partition(Collection<T> largeCollection, int partitionSize) {
		if (partitionSize <= 0) {
			throw new IllegalArgumentException("Illegal partitionSize, was: " + partitionSize);
		}
		if (largeCollection == null) {
			return null;
		}
		List<Collection<T>> retList = new LinkedList<Collection<T>>();
		Collection<T> part = null;
		for (T v : largeCollection) {
			if (part != null && part.size() == partitionSize) {
				retList.add(part);
				part = null;
			}
			if (part == null) {
				//Figure out what kind of collection this is, so that we don't break sorting etc
				if (largeCollection instanceof SortedSet) {
					part = new TreeSet<T>();
				} else if (largeCollection instanceof Set) {
					part = new HashSet<T>();
				} else if (largeCollection instanceof List) {
					part = new LinkedList<T>();
				} else {
					throw new UnsupportedOperationException("Not prepared for this type of collection: " + largeCollection.getClass().toString());
				}
			}
			part.add(v);
		}
		if (!isEmpty(part)) {
			retList.add(part);
		}
		return retList;
	}

	/**
	 * Simple method for generating a comma-separated string from a collection.
	 * @param collection
	 * @return A comma-separated string representation of the contents of the collection. Each element will be
	 * represented by it's toString method
	 */
	public static String toString(Collection<?> collection) {
		return toString(collection, ", ");
	}

	/**
	 * Simple method for generating a delimiter-separated string from a collection.
	 * @param collection
	 * @param delimiter
	 * @return A delimiter separated string representation of the contents of the collection. Each element will be
	 * represented by it's toString method
	 */
	public static String toString(Collection<?> collection, String delimiter) {
		if (collection == null) {
			return null;
		}
		if (collection.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		Iterator<?> iterator = collection.iterator();
		while (iterator.hasNext()) {
			Object t = iterator.next();
			sb.append(t.toString());
			if (iterator.hasNext()) {
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}
}