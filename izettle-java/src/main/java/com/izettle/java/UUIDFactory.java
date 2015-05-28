package com.izettle.java;

import java.util.Arrays;
import java.util.UUID;

/**
 * Creates and transforms URL safe Base64 encoded Universally Unique Identifier (UUID)
 * @see <a href="https://tools.ietf.org/html/rfc4122">https://tools.ietf.org/html/rfc4122</a>
 */
public final class UUIDFactory {

    static final int VERSION_TIME_BASED = 1;
    static final int VERSION_DCI_SECURITY = 2;
    static final int VERSION_NAME_BASED = 3;
    static final int VERSION_RANDOM = 4;

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
     * Creates an UUID version 1. The UUID does NOT contain any host information, but only time information.
     * The resulting string is guaranteed to be sufficiently unique to be usable as an id.
     * @return The UUID.
     */
    public static UUID createUUID1() {
        return UUID1Generator.generate();
    }

    /**
     * Creates a mutated URL safe (including removing trailing '=' characters) Base64 encoded UUID. The returned UUID
     * is based on the supplied one. This method is guaranteed to return a different UUID than the one supplied,
     * but done in a deterministic fashion (same value will be returned every time for each input value) and with the
     * same guarantee that the result is sufficiently unique to be used as a UUID.
     * Note, that for a time based UUID (type 1), the original time information will be kept intact. This might be
     * surprising, but there are really no logical alternatives (except for not allowing to create alternatives for this
     * type of UUID). As a consequence, the time information within the resulting UUID should not be used as a
     * substitute for information about when an event actually happened.
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
     * Note, that for a time based UUID (type 1), the original time information will be kept intact. This might be
     * surprising, but there are really no logical alternatives (except for not allowing to create alternatives for this
     * type of UUID). As a consequence, the time information within the resulting UUID should not be used as a
     * substitute for information about when an event actually happened.
     * @param uuid The original UUID.
     * @param mask Seed value to be used when producing the alternative. This value will be xor:ed
     *             with the original bytes to produce the alternative.
     * @return A new UUID that is based on the original UUID.
     */
    public static String createAlternative(String uuid, byte[] mask) {
        final UUID originalUuid = fromBase64String(uuid);
        final int originalVersion = originalUuid.version();
        final int originalVariant = originalUuid.variant();
        final long msb;
        switch (originalVersion) {
            case VERSION_TIME_BASED:
                //we don't want to change msb at all as that contains both version and clock data
                msb = originalUuid.getMostSignificantBits();
                break;
            case VERSION_RANDOM:
                byte[] msBytes = longToBytes(mask(originalUuid.getMostSignificantBits(), mask));
                //make sure to keep version and variant
                msBytes[6] &= 0x0f; //clear version
                msBytes[6] |= originalVersion << 4;
                msb = bytesToLong(msBytes);
                break;
            case VERSION_DCI_SECURITY:
            case VERSION_NAME_BASED:
            default:
                throw new UnsupportedOperationException("Cannot create alternative for UUID with version: " + originalVersion);
        }
        //We can always change the least significant bits
        final byte[] lsBytes = longToBytes(mask(originalUuid.getLeastSignificantBits(), mask));
        lsBytes[0] &= 0x3f; //clear variant
        lsBytes[0] |= originalVariant << 6;
        final long lsb = bytesToLong(lsBytes);
        return toBase64String(new UUID(msb, lsb));
    }

    private static long mask(long value, byte[] mask) {
        byte[] bytes = longToBytes(value);
        for (int i = 0; i < bytes.length; ++i) {
            bytes[i] ^= mask[i % mask.length];
        }
        return bytesToLong(bytes);
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
    public static String toBase64String(UUID uuid) {
        return toBase64String(toByteArray(uuid));
    }

    private static String toBase64String(byte[] bytes) {
        String result = Base64.byteArrToB64String(bytes);
        return result.split("=")[0]; // Remove trailing "=="
    }

    public static byte[] toByteArray(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("uuid cannot be null");
        }
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte[] buffer = new byte[16];
        System.arraycopy(longToBytes(msb), 0, buffer, 0, 8);
        System.arraycopy(longToBytes(lsb), 0, buffer, 8, 8);
        return buffer;
    }

    public static UUID fromBase64String(String b64) {
        if (b64 == null || b64.length() != 22) {
            throw new IllegalArgumentException("Argument b64 string must be defined and have a length of exactly 22");
        }
        return fromByteArray(Base64.b64StringToByteArr(b64));
    }

    public static UUID fromByteArray(byte[] bytes) {
        if (bytes == null || bytes.length != 16) {
            throw new IllegalArgumentException("Argument byte array must be defined and have a length of exactly 16");
        }
        long msb = bytesToLong(Arrays.copyOfRange(bytes, 0, 8));
        long lsb = bytesToLong(Arrays.copyOfRange(bytes, 8, 16));
        return new UUID(msb, lsb);
    }

    static long bytesToLong(byte[] bytes) {
        if (bytes == null || bytes.length != 8) {
            throw new IllegalArgumentException("Argument byte array must be defined and have a length of exactly 8");
        }
        long retVal = 0;
        for (int i = 0; i < 8; i++) {
            retVal += ((long) (bytes[i] & 0xFF)) << 8 * (7 - i);
        }
        return retVal;
    }

    static byte[] longToBytes(long value) {
        byte[] buffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            buffer[i] = (byte) (value >>> 8 * (7 - i));
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
            long midhiL = midhi;
            midhiL = (midhiL << 32) >>> 32; // to get rid of sign extension
            // and reconstruct
            long l1 = (((long) clockLo) << 32) | midhiL;
            // last detail: must force 2 MSB to be '10'
            return new UUID(l1, UUID.randomUUID().getLeastSignificantBits());
        }
    }
}
