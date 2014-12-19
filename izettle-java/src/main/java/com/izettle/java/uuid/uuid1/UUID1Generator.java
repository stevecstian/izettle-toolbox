package com.izettle.java.uuid.uuid1;

import java.util.UUID;

/**
 * Generates a new UUID version 1. The UUID does NOT contain any host information, but only time information.
 *
 * This class is based on the java-uuid-generator project.
 * @see <a href="https://github.com/cowtowncoder/java-uuid-generator">
 *     https://github.com/cowtowncoder/java-uuid-generator
 *     </a>
 */
public final class UUID1Generator {

	/**
	 * Used to transform UNIX timestamps to UUID timestamps (100ns
	 * unit offset from the beginning of Gregorian calendar).
	 */
	public static final long CLOCK_MULTIPLIER = 10000L;
	/**
	 * Used to transform UNIX timestamps to UUID timestamps (100ns
	 * unit offset from the beginning of Gregorian calendar).
	 */
	public static final long CLOCK_OFFSET = 0x01b21dd213814000L;

	private UUID1Generator() {
	}

	public static UUID generate() {
		long systemTime = System.currentTimeMillis();

		systemTime *= CLOCK_MULTIPLIER;
		systemTime += CLOCK_OFFSET;

		final long rawTimestamp = systemTime;

		// Time field components are kind of shuffled, need to slice:
		int clockHi = (int) (rawTimestamp >>> 32);
		int clockLo = (int) rawTimestamp;
		// and dice
		int midhi = (clockHi << 16) | (clockHi >>> 16);
		// need to squeeze in type (4 MSBs in byte 6, clock hi)
		midhi &= ~0xF000; // remove high nibble of 6th byte
		midhi |= 0x1000; // type 1
		long midhiL = (long) midhi;
		midhiL = (midhiL << 32) >>> 32; // to get rid of sign extension
		// and reconstruct
		long l1 = (((long) clockLo) << 32) | midhiL;
		// last detail: must force 2 MSB to be '10'
		return new UUID(l1, UUID.randomUUID().getLeastSignificantBits());
	}
}
