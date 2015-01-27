package com.izettle.java;

import java.util.UUID;

/**
 * Creates and transforms URL safe Base64 encoded Universally Unique Identifier (UUID)
 * @see <a href="https://tools.ietf.org/html/rfc4122">https://tools.ietf.org/html/rfc4122</a>
 */
public final class UUIDFactory {

    private UUIDFactory() {
    }

    /**
     * Creates a URL safe (including removing trailing '=' characters) Base64 encoded UUID version 4 as string. The
     * resulting string is guaranteed to be sufficiently unique to be usable as an id.
     * @return The encoded UUID as string.
     */
    public static String createUUID4AsString() {
        return toBase64String(UUID.randomUUID());
    }

    /**
     * Creates a URL safe (including removing trailing '=' characters) Base64 encoded UUID version 1 as string. The
     * UUID does NOT contain any host information, but only time information. The resulting string is guaranteed to be
     * sufficiently unique to be usable as an id.
     * @return The encoded UUID as string.
     */
    public static String createUUID1AsString() {
        return toBase64String(UUID1Generator.generate());
    }

    /**
     * Creates a mutated URL safe (including removing trailing '=' characters) Base64 encoded UUID. The returned UUID
     * is based on the supplied one. This method is guaranteed to return a different UUID than the one supplied,
     * but done in a deterministic fashion (same value will be returned every time for each input value) and with the
     * same guarantee that the result is sufficiently unique to be used as a UUID.
     * @param uuid The original UUID.
     * @return A new UUID that is based on the original UUID.
     */
    public static String createAlternative(String uuid) {
        return createAlternative(uuid, new byte[]{0x01});
    }

    /**
     * Creates a mutated URL safe (including removing trailing '=' characters) Base64 encoded UUID. The UUID is based
     * on the supplied one. This method is guaranteed to return a different UUID than the one supplied, but done in a
     * deterministic fashion (same value will be returned every time for each input value) and with the same guarantee
     * that the result is sufficiently unique to be used as a UUID.
     * @param uuid The original UUID.
     * @param mask Seed value to be used when producing the alternative. This value will be xor:ed
     *             with the original bytes to produce the alternative.
     * @return A new UUID that is based on the original UUID.
     */
    public static String createAlternative(String uuid, byte[] mask) {
        byte[] bytes = uuidToByteArray(uuid);
        for (int i = 0; i < bytes.length; ++i) {
            bytes[i] ^= mask[i % mask.length];
        }
        return toBase64String(bytes);
    }

    /**
     * Converts the supplied UUID to the bytes that represent it. This method supports both unencoded UUID strings and
     * Base64 encoded UUID strings (created by this factory).
     * @param uuid The supplied UUID as string.
     * @return The supplied UUID as byte array.
     */
    public static byte[] uuidToByteArray(String uuid) {
        // We assume that the supplied string is not Base64 encoded
        if (uuid.length() == 22) {
            return Base64.b64StringToByteArr(uuid);
            // We assume that the supplied string is already Base64 encoded
        } else {
            return uuid.getBytes();
        }
    }

    /**
     * Base64 encodes the supplied UUID into as URL safe format (including removing trailing '=' characters).
     * This will turn the UUID into a shorter format than what <code>.toString()</code> on the UUID would give.
     * For example, given the UUID of
     * <code>"cdaed56d-8712-414d-b346-01905d0026fe"</code>,
     * this method will return
     * <code>"za7VbYcSQU2zRgGQXQAm_g"</code>, which is a shorter representation of the same bytes.
     *
     * @param uuid The UUID to be converted.
     * @return The encoded UUID as string.
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

    /**
     * Generates a new UUID version 1. The UUID does NOT contain any host information, but only time information.
     *
     * This class is based on the java-uuid-generator project.
     * @see <a href="https://github.com/cowtowncoder/java-uuid-generator">
     *     https://github.com/cowtowncoder/java-uuid-generator
     *     </a>
     */
    private static class UUID1Generator {

        /**
         * Used to transform UNIX timestamps to UUID timestamps (100ns
         * unit offset from the beginning of Gregorian calendar).
         */
        private static final long CLOCK_MULTIPLIER = 10000L;
        /**
         * Used to transform UNIX timestamps to UUID timestamps (100ns
         * unit offset from the beginning of Gregorian calendar).
         */
        private static final long CLOCK_OFFSET = 0x01b21dd213814000L;

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
}
