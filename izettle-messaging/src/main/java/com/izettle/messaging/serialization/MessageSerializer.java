package com.izettle.messaging.serialization;

import static com.izettle.java.ValueChecks.isDefined;
import static com.izettle.java.ValueChecks.isEmpty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izettle.cryptography.KeyUtil;
import com.izettle.cryptography.PGP;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKey;

public class MessageSerializer<M> {
	private final PGPPublicKey publicKey;
	private final static ObjectMapper jsonMapper = new ObjectMapper();
	
	public MessageSerializer(byte[] publicPgpKey) throws PGPException {
		if (!isEmpty(publicPgpKey)) {
			try (InputStream publicPgpKeyInputStream = new ByteArrayInputStream(publicPgpKey)) {
				this.publicKey = KeyUtil.findPublicKey(publicPgpKeyInputStream);
			} catch (IOException e) {
				throw new PGPException("Could not create public PGP key", e);
			}
		} else {
			this.publicKey = null;
		}
	}
	
	public String encrypt(String message) throws IOException, PGPException {
		if (!isDefined(publicKey)) return message;
		
		return new String(PGP.encrypt(message.getBytes(), publicKey));
	}
	
	public String serialize(M message) throws JsonProcessingException {
		return jsonMapper.writeValueAsString(message);
	}
}
