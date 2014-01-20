package com.izettle.java;

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
	 * Creates a mutated uuid based on the supplied one. This method is guaranteed to return
	 * a different uuid than the one supplied, but done in a deterministic fashion (same value
	 * will be returned every time for each input value) and with the same guarantee that
	 * the result is sufficiently unique to be used as a uuid.
	 * @param uuid The original uuid.
	 * @return A new uuid that is based on the supplied one.
	 */
	public static String createAlternative(String uuid) {
		return createAlternative(uuid, new byte[]{0x01});
	}

	/**
	 * Creates a mutated uuid based on the supplied one. This method is guaranteed to return
	 * a different uuid than the one supplied, but done in a deterministic fashion (same value
	 * will be returned every time for each input value) and with the same guarantee that
	 * the result is sufficiently unique to be used as a uuid.
	 * @param uuid The original uuid.
	 * @param mask Seed value to be used when producing the alternative. This value will be xor:ed
	 *             with the original bytes to produce the alternative.
	 * @return A new uuid that is based on the supplied one.
	 */
	public static String createAlternative(String uuid, byte[] mask) {
		byte[] bytes = uuidToByteArray(uuid);
		for(int i = 0; i < bytes.length; ++i) {
			bytes[i] ^= mask[i % mask.length];
		}
		return toBase64String(bytes);
	}

	/**
	 * Converts the supplied uuid to the bytes that represent it. For uuids produced
	 * by <code>createAsString()</code>, the resulting array will be 16 bytes.
	 * @param uuid The original uuid.
	 * @return A new uuid that is based on the supplied one.
	 */
	public static byte[] uuidToByteArray(String uuid) {
		if (uuid.length() == 22) return Base64.b64StringToByteArr(uuid);
		return uuid.getBytes();
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
	static String toBase64String(UUID uuid) {
		return toBase64String(asByteArray(uuid));
	}

	private static String toBase64String(byte[] bytes) {
		String result = Base64.byteArrToB64String(bytes);
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
