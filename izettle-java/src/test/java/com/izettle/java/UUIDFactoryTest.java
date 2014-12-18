package com.izettle.java;

import static java.lang.Thread.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.UUID;
import org.junit.Test;

public class UUIDFactoryTest {
	private static final String UNENCODED_UUID4_STRING = "cdaed56d-8712-414d-b346-01905d0026fe";
	private static final String ENCODED_UUID4_STRING = "za7VbYcSQU2zRgGQXQAm_g";

	private static final String UNENCODED_UUID1_STRING = "f332d4f4-8694-11e4-9627-7c7a91d2d629";
	private static final String ENCODED_UUID1_STRING = "8zLU9IaUEeSWJ3x6kdLWKQ";

	@Test
	public void shouldCreateUUID4Successfully() {
		assertOnBase64String(UUIDFactory.createUUID4AsString());
	}

	@Test
	public void shouldCreateUUID1Successfully() {
		assertOnBase64String(UUIDFactory.createUUID1AsString());
	}

	@Test
	public void shouldShortenUUID4ToBase64EncodedStringSuccessfully() {
		String encodedUUIDString = UUIDFactory.toBase64String(UUID.fromString(UNENCODED_UUID4_STRING));

		assertOnBase64EncodedString(UNENCODED_UUID4_STRING, encodedUUIDString, ENCODED_UUID4_STRING);
	}

	@Test
	public void shouldShortenUUID1ToBase64StringSuccessfully() {
		String encodedUUIDString = UUIDFactory.toBase64String(UUID.fromString(UNENCODED_UUID1_STRING));

		assertOnBase64EncodedString(UNENCODED_UUID1_STRING, encodedUUIDString, ENCODED_UUID1_STRING);
	}

	@Test
	public void shouldReturnByteRepresentationOfBase64EncodedUUID4Successfully() {
		byte[] bytes = UUIDFactory.uuidToByteArray(ENCODED_UUID4_STRING);

		assertEquals(16, bytes.length);
	}

	@Test
	public void shouldReturnByteRepresentationOfUnencodedUUID4Successfully() {
		byte[] bytes = UUIDFactory.uuidToByteArray(UNENCODED_UUID4_STRING);

		assertEquals(36, bytes.length);
	}

	@Test
	public void shouldReturnByteRepresentationOfBase64EncodedUUID1Successfully() {
		byte[] bytes = UUIDFactory.uuidToByteArray(ENCODED_UUID1_STRING);

		assertEquals(16, bytes.length);
	}

	@Test
	public void shouldReturnByteRepresentationOfUnencodedUUID1Successfully() {
		byte[] bytes = UUIDFactory.uuidToByteArray(UNENCODED_UUID1_STRING);

		assertEquals(36, bytes.length);
	}

	@Test
	public void shouldCreateAlternativeVersionOfUUIDSuccessfully() {
		String uuid = UUIDFactory.createUUID4AsString();

		String alternativeUUID = UUIDFactory.createAlternative(uuid);

		assertOnAlternativeUUID(uuid, alternativeUUID);
	}

	@Test
	public void shouldCreateTheSameAlternativeVersionOfTheSameUUIDSuccessfully() {
		String uuid = UUIDFactory.createUUID4AsString();

		String firstAlternativeUUID = UUIDFactory.createAlternative(uuid);
		String secondsAlternativeUUID = UUIDFactory.createAlternative(uuid);

		assertEquals(secondsAlternativeUUID, firstAlternativeUUID);
	}

	@Test
	public void shouldExtractDateFromUUID1Successfully() throws InterruptedException {
		Date recentCurrentDate = new Date();

		// Make sure that at least 1 ms have passed so the dates will differ
		sleep(1);

		String uuidAsString = UUIDFactory.createUUID1AsString();
		byte[] uuidBytes = UUIDFactory.uuidToByteArray(uuidAsString);

		UUID uuid = createUUID1(uuidBytes);
		Date uuidDate = getDateFromUUID1(uuid);

		// Assert that the UUID date is after the recent current date
		assertTrue(uuidDate.compareTo(recentCurrentDate) > 0);
	}

	private void assertOnBase64String(String base64EncodedString) {
		assertNotNull(base64EncodedString);
		assertFalse(base64EncodedString.isEmpty());
		assertTrue(base64EncodedString.length() > 5);
		assertTrue(base64EncodedString.length() < 33);
	}

	private void assertOnBase64EncodedString(
		String unencodedUUIDString,
		String actualBase64EncodedString,
		String expectedBase64EncodedString
	) {
		assertNotNull(actualBase64EncodedString);
		assertFalse(actualBase64EncodedString.isEmpty());
		assertTrue(actualBase64EncodedString.length() < unencodedUUIDString.length());
		assertEquals(expectedBase64EncodedString, actualBase64EncodedString);
	}

	private void assertOnAlternativeUUID(String uuid, String alternativeUUID) {
		assertNotNull(alternativeUUID);
		assertEquals(22, uuid.length());
		assertEquals(22, alternativeUUID.length());
		assertNotEquals(uuid, alternativeUUID);
	}

	private UUID createUUID1(byte[] uuid1Bytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(uuid1Bytes);
		UUID uuid1 = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
		if (uuid1.version() != 1) {
			throw new IllegalArgumentException("The supplied byte array could not be converted into a UUID version 1");
		}
		return uuid1;
	}

	/**
	 * This extracts and converts the 64-bit UUID timestamp to a UNIX timestamp and finally returns it as a Date
	 * @param uuid1 The UUID to extracted from.
	 * @return The extracted date from the UUID.
	 */
	private static Date getDateFromUUID1(UUID uuid1) {
		long numberOf100NanosecondsIntervalsSinceUuidEpoch = 0x01b21dd213814000L;
		return new Date((uuid1.timestamp() - numberOf100NanosecondsIntervalsSinceUuidEpoch) / 10000);
	}
}
