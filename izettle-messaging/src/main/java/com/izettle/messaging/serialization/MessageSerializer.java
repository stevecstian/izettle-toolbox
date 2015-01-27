package com.izettle.messaging.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.izettle.cryptography.CryptographyException;

/**
 * Serialize a message to be published.
 * User: andreas
 * Date: 2014-07-16
 */
public interface MessageSerializer {

	/**
	 * Encrypt the message with the implementation specific cryptology method.
	 *
	 * @param message to be encrypted
	 * @return encrypted message
	 * @throws CryptographyException if encryption of message fail
	 */
	String encrypt(String message) throws CryptographyException;

	/**
	 * @param message to be serialized.
	 * @return serialized version of the message.
	 * @throws JsonProcessingException if the serialization fail.
	 */
	String serialize(Object message) throws JsonProcessingException;
}
