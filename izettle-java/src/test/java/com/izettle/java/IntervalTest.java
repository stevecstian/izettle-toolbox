package com.izettle.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import org.junit.Test;

public class IntervalTest {

    @Test
    public void itShouldFigureOutAbuts() throws Exception {
        final Instant instant1 = Instant.parse("2007-09-01T10:15:30.00Z");
        final Instant instant2 = Instant.parse("2013-10-02T10:15:30.00Z");
        final Instant instant3 = Instant.parse("2015-11-03T10:15:30.00Z");
        final Instant instant4 = Instant.parse("2017-12-03T10:15:30.00Z");
        final Interval interval1 = Interval.of(instant1, instant2);
        final Interval interval2 = Interval.of(instant2, instant3);
        final Interval interval3 = Interval.of(instant3, instant4);
        final Interval encompassing = Interval.of(instant1, instant4);
        final Interval zeroDuration = Interval.of(instant1, instant1);

        assertTrue(interval1.abuts(interval2));
        assertTrue(interval2.abuts(interval1));
        assertTrue(interval2.abuts(interval3));
        assertTrue(interval3.abuts(interval2));
        assertFalse(interval1.abuts(interval3));
        assertFalse(interval3.abuts(interval1));
        assertFalse(interval1.abuts(interval1));
        assertFalse(interval1.abuts(interval3));
        assertFalse(interval1.abuts(encompassing));
        assertTrue(zeroDuration.abuts(zeroDuration));
    }

    @Test
    public void itShouldFigureOutOverlaps() throws Exception {
        final Instant instant1 = Instant.parse("2007-09-01T10:15:30.00Z");
        final Instant instant2 = Instant.parse("2013-10-02T10:15:30.00Z");
        final Instant instant3 = Instant.parse("2015-11-03T10:15:30.00Z");
        final Instant instant4 = Instant.parse("2017-12-03T10:15:30.00Z");
        final Interval interval1 = Interval.of(instant1, instant2);
        final Interval interval2 = Interval.of(instant2, instant3);
        final Interval interval3 = Interval.of(instant3, instant4);
        final Interval encompassing = Interval.of(instant1, instant4);
        final Interval zeroDuration = Interval.of(instant1, instant1);

        assertFalse(interval1.overlap(interval2).isPresent());
        assertFalse(interval1.overlap(interval3).isPresent());
        assertTrue(interval1.overlap(encompassing).isPresent());
        assertFalse(interval1.overlap(zeroDuration).isPresent());
        assertEquals(interval3, encompassing.overlap(interval3).get());
    }

    @Test
    public void itShouldFigureOutContains() throws Exception {
        final Instant start = Instant.now();
        final Instant end = start.plus(Duration.ofHours(1L));
        final Interval interval = Interval.of(start, end);

        assertFalse(interval.contains(start.minus(Duration.ofMillis(1L))));
        assertTrue(interval.contains(start));
        assertTrue(interval.contains(start.plus(Duration.ofMillis(1L))));
        assertFalse(interval.contains(end.plus(Duration.ofMillis(1L))));
        assertFalse(interval.contains(end));
        assertTrue(interval.contains(end.minus(Duration.ofMillis(1L))));
    }

    @Test
    public void itShouldFigureOutDisjoint() throws Exception {
        final Instant instant1 = Instant.parse("2007-09-01T10:15:30.00Z");
        final Instant instant2 = Instant.parse("2013-10-02T10:15:30.00Z");
        final Instant instant3 = Instant.parse("2015-11-03T10:15:30.00Z");
        final Instant instant4 = Instant.parse("2017-12-03T10:15:30.00Z");
        final Interval interval1 = Interval.of(instant1, instant2);
        final Interval interval2 = Interval.of(instant2, instant3);
        final Interval interval3 = Interval.of(instant3, instant4);
        final Interval encompassing = Interval.of(instant1, instant4);
        final Interval zeroDuration = Interval.of(instant1, instant1);

        assertFalse(interval1.disjoint(interval2));
        assertTrue(interval1.disjoint(interval3));
        assertFalse(interval1.disjoint(encompassing));
        assertFalse(interval1.disjoint(zeroDuration));
    }

    @Test
    public void itShouldReturnCorrectDuration() throws Exception {
        final Instant start = Instant.parse("2013-10-02T10:15:30.00Z");
        final Instant end = Instant.parse("2013-10-02T10:15:31.00Z");
        final Interval interval = Interval.of(start, end);

        assertEquals(Duration.ofSeconds(1L), interval.getDuration());
    }
}
