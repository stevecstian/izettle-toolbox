package com.izettle.java.testutils;

import java.util.Date;
import java.util.UUID;

public final class UUIDUtils {

	/** Used to transform UNIX timestamps to UUID timestamps (100ns
	 * unit offset from the beginning of Gregorian calendar).
	 */
	private static final long CLOCK_MULTIPLIER = 10000L;
	/**
	 * Used to transform UNIX timestamps to UUID timestamps (100ns
	 * unit offset from the beginning of Gregorian calendar).
	 */
	private static final long CLOCK_OFFSET = 0x01b21dd213814000L;

	private UUIDUtils() {
	}

	/**
	 * Extracts a UUID timestamp and returns it as a Date.
	 * @param uuid1 The UUID to extract from.
	 * @return The extracted date.
	 */
	public static Date getDateFromUUID1(UUID uuid1) {
		return new Date((uuid1.timestamp() - CLOCK_OFFSET) / CLOCK_MULTIPLIER);
	}
}