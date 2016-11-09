package com.izettle.java;

import static com.izettle.java.CollectionUtils.partition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.internal.util.collections.Sets;

public class CollectionUtilsSpec {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void shouldPartition() {
        Set<String> set = Sets.newSet("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11");
        List<Collection<String>> partitions1 = partition(set, 1);
        assertTrue(partitions1.size() == 11);
        assertTrue(partitions1.get(0).size() == 1);
        List<Collection<String>> partitions2 = partition(set, 7);
        assertTrue(partitions2.size() == 2);
        assertTrue(partitions2.get(0).size() == 7);
        assertTrue(partitions2.get(1).size() == 4);
    }

    @Test
    public void testPartition_badSize() {
        Set<Integer> source = Sets.newSet(1);
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("Illegal partitionSize, was: 0");
        partition(source, 0);
    }

    @Test
    public void testPartition_empty() {
        Set<Integer> source = Collections.emptySet();
        List<Collection<Integer>> partitions = partition(source, 1);
        assertTrue(partitions.isEmpty());
        assertEquals(0, partitions.size());
    }

    @Test
    public void testPartition_null() throws Exception {
        List<Collection<Integer>> partitions = partition(null, 1);
        assertNull(partitions);
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
        Collection<Integer> collection1 = new HashSet<>(Arrays.asList(6, 5, 4, 3));
        //Just verify that the result is a set
        List<Collection<Integer>> parts1 = partition(collection1, 2);
        assertTrue(parts1.get(0) instanceof Set);

        Collection<Integer> collection2 = new TreeSet<>(Arrays.asList(6, 5, 4, 3));
        //Verify that the result is a set
        List<Collection<Integer>> parts2 = partition(collection2, 2);
        assertTrue(parts2.get(0) instanceof Set);
        //Also verify that the natural ordering is respected
        assertTrue(parts2.get(0).iterator().next() == 3);
        assertTrue(parts2.get(1).iterator().next() == 5);

        Collection<Integer> collection3 = new ArrayList<>(Arrays.asList(6, 5, 4, 3));
        collection3.add(6);
        collection3.add(5);
        collection3.add(4);
        collection3.add(3);
        //Verify that the result is a list
        List<Collection<Integer>> parts3 = partition(collection3, 2);
        assertTrue(parts3.get(0) instanceof List);
        //Also verify that the original ordering is respected
        assertTrue(parts3.get(0).iterator().next() == 6);
        assertTrue(parts3.get(1).iterator().next() == 4);
    }

    @Test
    public void itShouldThrowExceptionForUnhandledCollectionType() {
        Collection<Integer> collection = new PriorityQueue<>(Arrays.asList(6, 5, 4, 3));
        thrown.expect(UnsupportedOperationException.class);
        thrown.expectMessage("Not prepared for this type of collection: class java.util.PriorityQueue");
        partition(collection, 2);
    }

    @Test
    public void itShouldToStringProperly() {
        Set<String> set = new TreeSet<>();
        set.add("A");
        set.add("B");
        set.add("C");
        assertEquals("A, B, C", CollectionUtils.toString(set));
    }

    @Test
    public void itShouldToStringEmpty() {
        Set<String> set = new TreeSet<>();
        assertEquals("", CollectionUtils.toString(set));
    }

    @Test
    public void itShouldToStringNull() {
        assertNull(null, CollectionUtils.toString(null));
    }

    @Test
    public void itShouldHandleNullElements() {
        assertEquals(null, CollectionUtils.toString(Collections.singletonList(null)), "null");
    }
}
