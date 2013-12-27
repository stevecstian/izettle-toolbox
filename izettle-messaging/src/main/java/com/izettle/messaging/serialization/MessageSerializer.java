package com.izettle.messaging.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izettle.cryptography.CryptographyException;
import com.izettle.cryptography.KeyUtil;
import com.izettle.cryptography.PGP;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.openpgp.PGPPublicKey;

public class MessageSerializer<M> {

	private final PGPPublicKey publicKey;
	private final static ObjectMapper jsonMapper = JsonSerializer.getInstance();

	public MessageSerializer(byte[] publicPgpKey) throws CryptographyException {
		try (InputStream publicPgpKeyInputStream = new ByteArrayInputStream(publicPgpKey)) {
			this.publicKey = KeyUtil.findPublicKey(publicPgpKeyInputStream);
		} catch (IOException e) {
			throw new CryptographyException("Could not create public PGP key", e);
		}
	}

	public MessageSerializer() {
		this.publicKey = null;
	}

	public String encrypt(String message) throws CryptographyException {
		if (publicKey == null) {
			return message;
		}

		return new String(PGP.encrypt(message.getBytes(), publicKey));
	}

	public String serialize(M message) throws JsonProcessingException {
		return jsonMapper.writeValueAsString(message);
	}
}
