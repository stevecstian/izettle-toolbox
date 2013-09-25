package com.izettle.toolbox.java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
}
