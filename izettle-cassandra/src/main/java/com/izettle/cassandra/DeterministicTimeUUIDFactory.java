package com.izettle.cassandra;

import java.util.Date;
import java.util.UUID;

public class DeterministicTimeUUIDFactory {

    /**
     * Creates a time based deterministic UUID based on a given seed string and date.
     *
     * @param seedString A string to base this time UUID on.
     * @param date Date part of the UUID.
     * @return Time based UUID.
     */
    public static UUID create(String seedString, Date date) {
        long seedUUIDBits = seedString.hashCode();
        long dateBits = createDateBits(date.getTime());
        return new UUID(dateBits, seedUUIDBits);
    }

    /**
     * Creates a time based deterministic UUID based on a given seed UUID and date.
     *
     * @param seedUUID A UUID to base this time UUID on.
     * @param date Date part of the UUID.
     * @return Time based UUID.
     */
    public static UUID create(UUID seedUUID, Date date) {
        long seedUUIDBits = seedUUID.getLeastSignificantBits();
        long dateBits = createDateBits(date.getTime());
        return new UUID(dateBits, seedUUIDBits);
    }

    /**
     * Creates the lexigraphicaly lowest UUID for a date.
     *
     * @param date Date part of the UUID.
     * @return Time based UUID.
     */
    public static UUID createFirst(Date date) {
        long dateBits = createDateBits(date.getTime());
        return new UUID(dateBits, 0);
    }

    /**
     * Creates the lexigraphicaly highest UUID for a date.
     *
     * @param date Date part of the UUID.
     * @return Time based UUID.
     */
    public static UUID createLast(Date date) {
        long dateBits = createDateBits(date.getTime());
        return new UUID(dateBits, Long.MAX_VALUE);
    }

    /**
     * Highly influenced by: https://github.com/cowtowncoder/java-uuid-generator/blob/3.0/src/main/java/com/fasterxml/uuid/impl/TimeBasedGenerator.java
     *
     * @param timestamp
     * @return
     */
    private static long createDateBits(long timestamp) {
        // Time field components are kind of shuffled, need to slice:
        int clockHi = (int) (timestamp >>> 32);
        int clockLo = (int) timestamp;
        // and dice
        int midhi = (clockHi << 16) | (clockHi >>> 16);
        // need to squeeze in type (4 MSBs in byte 6, clock hi)
        midhi &= ~0xF000; // remove high nibble of 6th byte
        midhi |= 0x1000; // type 1
        long midhiL = (long) midhi;
        midhiL = (midhiL << 32) >>> 32; // to get rid of sign extension
        // and reconstruct
        // last detail: must force 2 MSB to be '10'
        return (((long) clockLo) << 32) | midhiL;
    }
}
