package com.izettle.messaging.serialization;

import static org.junit.Assert.assertEquals;

import com.izettle.java.DateFormatCreator;
import com.izettle.java.ResourceUtils;
import com.izettle.java.TimeZoneId;
import com.izettle.messaging.TestMessage;
import com.izettle.messaging.TestMessageWithDate;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class MessageDeserializerTest {

	private MessageDeserializer<TestMessage> plaintextDeserializer;
	private String plaintextMessage;

	@Before
	public void setup() throws IOException {
		plaintextDeserializer = new MessageDeserializer<>(TestMessage.class);
		plaintextMessage = new String(ResourceUtils.getResourceAsBytes("example-message.json"));
	}

	@Test
	public void decryptUsingPlaintextDeserializerShouldDoNothing() throws Exception {
		String decrypted = plaintextDeserializer.decrypt(plaintextMessage);
		assertEquals(plaintextMessage, decrypted);
	}

	@Test
	public void deserializingPlaintextMessageShouldResultInValidObject() throws Exception {
		TestMessage msg = plaintextDeserializer.deserialize(plaintextMessage);
		assertEquals("message in a bottle", msg.getMessage());
	}

	@Test
	public void deserializingMessageWithRfc3339DateShouldParseDateCorrectly() throws Exception {
		// Arrange
		String json = "{\"date\":\"2001-12-23T03:05:06.123+0100\"}";

		// Act
		TestMessageWithDate msg = new MessageDeserializer<>(TestMessageWithDate.class).deserialize(json);

		// Assert
		String dateFieldAsString = DateFormatCreator.createDateAndTimeMillisFormatter(TimeZoneId.UTC)
				.format(msg.getDate());
		assertEquals("2001-12-23 02:05:06.123", dateFieldAsString);
	}
}
