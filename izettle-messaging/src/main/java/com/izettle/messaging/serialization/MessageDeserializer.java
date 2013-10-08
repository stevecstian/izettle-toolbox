package com.izettle.messaging.serialization;

import static com.izettle.java.ValueChecks.isDefined;
import static com.izettle.java.ValueChecks.areDefined;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izettle.cryptography.CryptographyException;
import com.izettle.cryptography.PGP;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.bouncycastle.openpgp.PGPException;

public class MessageDeserializer<M> {
	private final byte[] privatePgpKey;
	private final String privatePgpKeyPassphrase;
	private final static ObjectMapper jsonMapper = new ObjectMapper();
	private final Class<M> messageClass;

	public MessageDeserializer(Class<M> messageClass, byte[] privatePgpKey, final String privatePgpKeyPassphrase) throws PGPException {
		this.privatePgpKey = privatePgpKey;
		this.privatePgpKeyPassphrase = privatePgpKeyPassphrase;
		this.messageClass = messageClass;
	}

	public String decrypt(String encrypted) throws CryptographyException, UnsupportedEncodingException {
		if (areDefined(privatePgpKey, privatePgpKeyPassphrase)) {
			final ByteArrayInputStream keyStream = new ByteArrayInputStream(privatePgpKey);
			return new String(PGP.decrypt(encrypted.getBytes(), keyStream, privatePgpKeyPassphrase), "UTF-8");
		}
		return encrypted;
	}

	public M deserialize(String message) throws IOException, PGPException {
		return jsonMapper.readValue(message, messageClass);
	}

	public static String removeSnsEnvelope(String message) throws IOException {
		if (isDefined(message) && message.startsWith("{")) {
			JsonNode root = jsonMapper.readTree(message);
			if (root.has("TopicArn") && root.has("Message")) {
				return root.get("Message").asText();
			}
		}
		return message; // Message is most likely not from SNS.
	}
}
