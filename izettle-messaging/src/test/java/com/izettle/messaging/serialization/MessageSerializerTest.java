package com.izettle.messaging.serialization;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.izettle.cryptography.CryptographyException;
import com.izettle.java.ResourceUtils;
import com.izettle.messaging.TestMessage;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class MessageSerializerTest {

	private MessageSerializer<TestMessage> plaintextSerializer;
	private MessageSerializer<TestMessage> pgpSerializer;
	private MessageDeserializer<TestMessage> pgpDeserializer;
	private String plaintextMessage;

	@Before
	public void setup() throws IOException, CryptographyException {
		plaintextSerializer = new MessageSerializer<>();
		plaintextMessage = new String(ResourceUtils.getResourceAsBytes("example-message.json")).trim();

		// key produced by:                  gpg --gen-key
		// public key exported to file by:   gpg --export --armor >pgp-example-public.key
		// private key exported to file by:  gpg --export-secret-keys --armor >pgp-example-private.key
		pgpSerializer = new MessageSerializer<>(ResourceUtils.getResourceAsBytes("pgp-example-public.key"));
		pgpDeserializer = new MessageDeserializer<>(TestMessage.class, ResourceUtils.getResourceAsBytes("pgp-example-private.key"), "example");
	}

	@Test
	public void encryptUsingPlaintextSerializerShouldDoNothing() throws Exception {
		String encrypted = plaintextSerializer.encrypt(plaintextMessage);
		assertEquals(plaintextMessage, encrypted);
	}

	@Test
	public void encryptUsingPgpSerializerShouldEncryptContents() throws Exception {
		String encrypted = pgpSerializer.encrypt(plaintextMessage);
		assertNotNull(encrypted);
		assertTrue(encrypted.length() > 20);
		assertNotEquals(plaintextMessage, encrypted);
	}

	@Test
	public void encryptingAndSerializingUsingPlaintextSerializerShouldResultInReadableJson() throws Exception {
		TestMessage msg = new TestMessage("message in a bottle");
		String serialized = plaintextSerializer.serialize(msg);
		String encryptedBody = plaintextSerializer.encrypt(serialized);
		assertEquals(plaintextMessage, encryptedBody);
	}

	@Test
	public void encryptingAndSerializingUsingPgpSerializerShouldResultInUnreadableJson() throws Exception {
		TestMessage msg = new TestMessage("message in a bottle");
		String serialized = pgpSerializer.serialize(msg);
		String encryptedBody = pgpSerializer.encrypt(serialized);
		assertNotNull(encryptedBody);
		assertTrue(encryptedBody.length() > 20);
		assertNotEquals(plaintextMessage, encryptedBody);
	}

	@Test
	public void encryptAndDecryptUsingPgpSerializersShouldResultInTheSameMessage() throws Exception {
		TestMessage msg = new TestMessage("message in a bottle");
		String serialized = pgpSerializer.serialize(msg);
		String encryptedBody = pgpSerializer.encrypt(serialized);
		String decryptedBody = pgpDeserializer.decrypt(encryptedBody);
		TestMessage deserializedMessage = pgpDeserializer.deserialize(decryptedBody);
		assertEquals(msg.getMessage(), deserializedMessage.getMessage());
	}
}
