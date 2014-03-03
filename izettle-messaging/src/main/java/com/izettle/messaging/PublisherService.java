package com.izettle.messaging;

import static com.izettle.java.ValueChecks.empty;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.izettle.cryptography.CryptographyException;
import com.izettle.messaging.serialization.MessageSerializer;
import java.io.IOException;
import java.util.Collection;

/**
 * Convenience class for using Amazon Simple Notification Service.
 */
public class PublisherService implements MessagePublisher {

	private final String topicArn;
	private final AmazonSNS amazonSNS;
	private final MessageSerializer messageSerializer;

	public static MessagePublisher nonEncryptedPublisherService(AmazonSNS client, final String topicArn) {
		return new PublisherService(client, topicArn);
	}

	public static <T> MessagePublisher encryptedPublisherService(AmazonSNS client, final String topicArn, final byte[] publicPgpKey) throws MessagingException {
		if (empty(publicPgpKey)) {
			throw new MessagingException("Can't create encryptedPublisherService with null as public PGP key");
		}
		return new PublisherService(client, topicArn, publicPgpKey);
	}

	private PublisherService(AmazonSNS client, String topicArn) {
		this.amazonSNS = client;
		this.topicArn = topicArn;
		this.messageSerializer = new MessageSerializer();
	}

	private PublisherService(AmazonSNS client, String topicArn, byte[] publicPgpKey) throws MessagingException {
		this.amazonSNS = client;
		this.topicArn = topicArn;
		try {
			this.messageSerializer = new MessageSerializer(publicPgpKey);
		} catch (CryptographyException e) {
			throw new MessagingException("Failed to load public PGP key needed to encrypt messages.", e);
		}
	}

	/**
	 * Posts message to queue.
	 *
	 * @param message Message to post.
	 * @param eventName Message subject (type of message).
	 * @throws MessagingException Failed to post message.
	 */
	@Override
	public <M> void post(M message, String eventName) throws MessagingException {
		if (empty(eventName)) {
			throw new MessagingException("Cannot publish message with empty eventName!");
		}
		try {
			String jsonBody = messageSerializer.serialize(message);
			String encryptedBody = messageSerializer.encrypt(jsonBody);
			PublishRequest publishRequest = new PublishRequest(topicArn, encryptedBody, eventName);
			amazonSNS.publish(publishRequest);
		} catch (IOException | CryptographyException e) {
			throw new MessagingException("Failed to publish message " + eventName, e);
		}
	}
	/**
	 * Posts several messages to topic.
	 *
	 * @param messages Messages to post.
	 * @param eventName Message subject (type of message).
	 * @throws MessagingException Failed to post at least one of the messages.
	 */
	@Override
	public <M> void postBatch(Collection<M> messages, String eventName) throws MessagingException {
		for (M message : messages) {
			post(message, eventName);
		}
	}
}
