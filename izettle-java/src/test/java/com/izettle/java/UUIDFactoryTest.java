package com.izettle.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import org.junit.Test;

public class UUIDFactoryTest {

	@Test
	public void testShouldCreateSimpleUniqueIdentifier() {
		// Arrange
		// Act
		String s = UUIDFactory.createAsString();

		// Assert
		assertNotNull(s);
		assertFalse(s.isEmpty());
		assertTrue(s.length() > 5);
		assertTrue(s.length() < 33);
	}

	@Test
	public void testCanShortenUUIDsToB64Strings() {
		// Arrange
		String originalString = "cdaed56d-8712-414d-b346-01905d0026fe";
		UUID uuid = UUID.fromString(originalString);

		// Act
		String s = UUIDFactory.toBase64String(uuid);

		// Assert
		assertNotNull(s);
		assertFalse(s.isEmpty());
		assertTrue(s.length() < originalString.length());
		assertEquals("za7VbYcSQU2zRgGQXQAm_g", s);
	}

	@Test
	public void uuidsCanBeDeconstructedToByteArrays() {
		// We should be able to create a byte array from a uuid string.
		byte[] bytes1 = UUIDFactory.uuidToByteArray(UUIDFactory.createAsString());
		assertEquals(16, bytes1.length);

		byte[] bytes2 = UUIDFactory.uuidToByteArray("notProducedByCreateAsString");
		assertEquals(27, bytes2.length);

		byte[] bytes3 = UUIDFactory.uuidToByteArray("123456789");
		assertEquals(9, bytes3.length);
	}

	@Test
	public void canCreateAlternativeVersionsOfUUIDs() {
		// Arrange
		String uuid = UUIDFactory.createAsString();
		assertEquals(22, uuid.length());

		// Act
		String maskedUUID = UUIDFactory.createAlternative(uuid);

		// Assert
		assertNotNull(maskedUUID);
		assertEquals(22, maskedUUID.length());
		assertNotEquals(uuid, maskedUUID);
	}
}
