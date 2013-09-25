package com.izettle.toolbox;

import java.util.UUID;

public class UUIDFactory {

	private UUIDFactory() {
	}

	/**
	 * Creates a Universally Unique Identifier, as string. The resulting string is guaranteed to
	 * be sufficiently unique to be usable as an id. This method currently returns base64-encoded
	 * strings of random UUID bytes. An example of a returned id is "za7VbYcSQU2zRgGQXQAm_g".
	 *
	 * @return
	 */
	public static String createAsString() {
		return toBase64String(UUID.randomUUID());
	}

	/**
	 * Converts the supplied UUID into a shorter format than what <code>.toString()</code>
	 * on the UUID would give.
	 * For example, given the UUID of
	 * <code>"cdaed56d-8712-414d-b346-01905d0026fe"</code>,
	 * this method will return
	 * <code>"za7VbYcSQU2zRgGQXQAm_g"</code>, which is a shorter representation of the same bytes.
	 *
	 * @param uuid
	 * @return A compact string representation of the bytes from the specified UUID.
	 */
	private static String toBase64String(UUID uuid) {
		String result = Base64.byteArrToB64String(asByteArray(uuid));
		return result.split("=")[0]; // Remove trailing "=="
	}

	private static byte[] asByteArray(UUID uuid) {
		long msb = uuid.getMostSignificantBits();
		long lsb = uuid.getLeastSignificantBits();
		byte[] buffer = new byte[16];
		for (int i = 0; i < 8; i++) {
			buffer[i] = (byte) (msb >>> 8 * (7 - i));
		}
		for (int i = 8; i < 16; i++) {
			buffer[i] = (byte) (lsb >>> 8 * (7 - i));
		}
		return buffer;
	}
}
