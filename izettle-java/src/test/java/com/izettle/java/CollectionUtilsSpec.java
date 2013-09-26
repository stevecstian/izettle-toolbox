package com.izettle.java;

import static com.izettle.java.CollectionUtils.partition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import org.mockito.internal.util.collections.Sets;

/**
 *
 * @author adam
 */
public class CollectionUtilsSpec {

	@Test
	public void shouldPartition() {
		Set<String> set = Sets.newSet("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
		List<Collection<String>> partitions = partition(set, 1);
		assertTrue(partitions.size() == 11);
		assertTrue(partitions.get(0).size() == 1);
		partitions = partition(set, 7);
		assertTrue(partitions.size() == 2);
		assertTrue(partitions.get(0).size() == 7);
		assertTrue(partitions.get(1).size() == 4);
	}

	@Test
	public void testPartition_badSize() {
		Set<Integer> source = Sets.newSet(1);
		try {
			partition(source, 0);
			fail("bad size");
		} catch (IllegalArgumentException expected) {
		}
	}

	@Test
	public void testPartition_empty() {
		Set<Integer> source = Collections.emptySet();
		List<Collection<Integer>> partitions = partition(source, 1);
		assertTrue(partitions.isEmpty());
		assertEquals(0, partitions.size());
	}

	@Test
	public void testPartition_1_1() {
		Set<Integer> source = Sets.newSet(1);
		List<Collection<Integer>> partitions = partition(source, 1);
		assertEquals(1, partitions.size());
		assertEquals(Sets.newSet(1), partitions.get(0));
	}

	@Test
	public void testPartition_1_2() {
		Set<Integer> source = Sets.newSet(1);
		List<Collection<Integer>> partitions = partition(source, 2);
		assertEquals(1, partitions.size());
		assertEquals(Sets.newSet(1), partitions.get(0));
	}

	@Test
	public void testPartition_2_1() {
		Set<Integer> source = Sets.newSet(1, 2);
		List<Collection<Integer>> partitions = partition(source, 1);
		assertEquals(2, partitions.size());
		assertEquals(Sets.newSet(1), partitions.get(0));
		assertEquals(Sets.newSet(2), partitions.get(1));
	}

	@Test
	public void testPartition_3_2() {
		Set<Integer> source = Sets.newSet(1, 2, 3);
		List<Collection<Integer>> partitions = partition(source, 2);
		assertEquals(2, partitions.size());
		assertEquals(Sets.newSet(1, 2), partitions.get(0));
		assertEquals(Sets.newSet(3), partitions.get(1));
	}

	@Test
	public void testPartitionSize_1() {
		Set<Integer> list = Sets.newSet(1, 2, 3);
		assertEquals(1, partition(list, Integer.MAX_VALUE).size());
		assertEquals(1, partition(list, Integer.MAX_VALUE - 1).size());
	}

	@Test
	public void testPartitionSize_2_2() {
		Set<Integer> list = Sets.newSet(1, 2, 3, 4);
		assertEquals(2, partition(list, 2).get(0).size());
		assertEquals(2, partition(list, 2).get(1).size());
	}

	@Test
	public void itShouldPartitionCorrectCollectionClass() {
		Collection<Integer> collection = new HashSet<Integer>(Arrays.asList(6, 5, 4, 3));
		//Just verify that the result is a set
		List<Collection<Integer>> parts = partition(collection, 2);
		assertTrue(parts.get(0) instanceof Set);

		collection = new TreeSet<Integer>(Arrays.asList(6, 5, 4, 3));
		//Verify that the result is a set
		parts = partition(collection, 2);
		assertTrue(parts.get(0) instanceof Set);
		//Also verify that the natural ordering is respected
		assertTrue(parts.get(0).iterator().next() == 3);
		assertTrue(parts.get(1).iterator().next() == 5);

		collection = new ArrayList<Integer>(Arrays.asList(6, 5, 4, 3));
		collection.add(6);
		collection.add(5);
		collection.add(4);
		collection.add(3);
		//Verify that the result is a list
		parts = partition(collection, 2);
		assertTrue(parts.get(0) instanceof List);
		//Also verify that the original ordering is respected
		assertTrue(parts.get(0).iterator().next() == 6);
		assertTrue(parts.get(1).iterator().next() == 4);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void itShouldThrowExceptionForUnhandledCollectionType() {
		Collection<Integer> collection = new PriorityQueue<Integer>(Arrays.asList(6, 5, 4, 3));
		List<Collection<Integer>> parts = partition(collection, 2);
	}

	@Test
	public void itShouldToStringProperly() {
		Set<String> set = new TreeSet<String>();
		set.add("A");
		set.add("B");
		set.add("C");
		assertEquals("A, B, C", CollectionUtils.toString(set));
	}

	@Test
	public void itShouldToStringEmpty() {
		Set<String> set = new TreeSet<String>();
		assertEquals("", CollectionUtils.toString(set));
	}

	@Test
	public void itShouldToStringNull() {
		assertNull(null, CollectionUtils.toString(null));
	}
}
