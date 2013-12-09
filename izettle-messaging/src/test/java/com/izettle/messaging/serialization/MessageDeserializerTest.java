package com.izettle.messaging.serialization;

import static org.junit.Assert.assertEquals;

import com.izettle.java.ResourceUtils;
import com.izettle.messaging.TestMessage;
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
}
