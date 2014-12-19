package com.izettle.java.uuid.uuid1;

import static com.izettle.java.uuid.uuid1.UUID1Generator.*;
import static com.izettle.java.uuid.uuid1.UUID1Generator.CLOCK_OFFSET;

import java.util.Date;
import java.util.UUID;

public final class UUID1Utils {

	private UUID1Utils() {
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
