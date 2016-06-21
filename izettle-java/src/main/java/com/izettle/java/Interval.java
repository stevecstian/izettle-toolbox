package com.izettle.java;

import static java.util.Objects.requireNonNull;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a period on the time line between two instants. An interval will always be inclusive at the start and
 * exclusive at the end
 */
public class Interval {

    private final Instant start;
    private final Instant end;

    public static Interval of(final Instant start, final Instant end) {
        return new Interval(start, end);
    }

    private Interval(final Instant start, final Instant end) {
        this.start = requireNonNull(start, "Start instant cannot be null");
        this.end = requireNonNull(end, "End instant cannot be null");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("An interval cannot have an end that's before it's start");
        }
    }

    /**
     * Specifies whether the given instant is inside this interval or not.
     * @param instant the instant to check
     * @return true if the instant is inside the interval, otherwise false
     */
    public boolean contains(final Instant instant) {
        requireNonNull(instant, "Instant cannot be null");
        return start.equals(instant) || (instant.isAfter(start) && instant.isBefore(end));
    }

    /**
     * Calculates the overlap between this and the other interval, and returns the result as a new Interval
     * @param other the other interval to check overlap against, never null
     * @return an Optional with a present value if the two intervals overlaps, or absent otherwise. Never null.
     */
    public Optional<Interval> overlap(final Interval other) {
        requireNonNull(other, "Other interval cannot be null");
        if (disjoint(other) || abuts(other)) {
            return Optional.empty();
        }
        return Optional.of(
            new Interval(
                start.isAfter(other.start) ? start : other.start,
                end.isBefore(other.end) ? end : other.end
            )
        );
    }

    /**
     * Figures out whether the other interval abuts this interval in either end.
     * @param other the other interval to compare with. Cannot be null
     * @return true if this interval ends just were the other starts, or starts where the other ends. false otherwise
     */
    public boolean abuts(final Interval other) {
        requireNonNull(other, "other interval cannot be null");
        return start.equals(other.end) || other.start.equals(end);
    }

    /**
     * Figures out whether the other interval is completely before or completely after this interval
     * @param other the other interval to compare with. Cannot be null
     * @return true if the other interval is completely before or completely after this interval
    */
    public boolean disjoint(final Interval other) {
        requireNonNull(other, "other interval cannot be null");
        return start.isAfter(other.end) || other.start.isAfter(end);
    }

    public Duration getDuration() {
        return Duration.between(start, end);
    }

    public Instant getStart() {
        return start;
    }

    public Instant getEnd() {
        return end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Interval other = (Interval) obj;
        if (!this.start.equals(other.start)) {
            return false;
        }
        return this.end.equals(other.end);
    }

}
