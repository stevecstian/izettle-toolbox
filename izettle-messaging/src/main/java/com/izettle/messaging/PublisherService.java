package com.izettle.messaging;

import static com.izettle.java.ValueChecks.coalesce;
import static com.izettle.java.ValueChecks.empty;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.izettle.cryptography.CryptographyException;
import com.izettle.messaging.serialization.MessageSerializer;
import java.io.IOException;

/**
 * Convenience class for using Amazon Simple Notification Service.
 *
 * @param <M> Message type.
 */
public class PublisherService<M> implements MessageQueueProducer<M> {

	private final String topicArn;
	private final AmazonSNS amazonSNS;
	private final String eventName;
	private final MessageSerializer<M> messageSerializer;

	public static <T> MessageQueueProducer<T> nonEncryptedPublisherService(AmazonSNS client, final String topicArn) {
		return nonEncryptedPublisherService(client, topicArn, null);
	}

	public static <T> MessageQueueProducer<T> nonEncryptedPublisherService(AmazonSNS client, final String topicArn, String eventName) {
		return new PublisherService<>(client, topicArn, eventName);
	}

	public static <T> MessageQueueProducer<T> encryptedPublisherService(AmazonSNS client, final String topicArn, final byte[] publicPgpKey) throws MessagingException {
		return encryptedPublisherService(client, topicArn, null, publicPgpKey);
	}

	public static <T> MessageQueueProducer<T> encryptedPublisherService(AmazonSNS client, final String topicArn, String eventName, final byte[] publicPgpKey) throws MessagingException {
		if (empty(publicPgpKey)) {
			throw new MessagingException("Can't create encryptedPublisherService with null as public PGP key");
		}
		return new PublisherService<>(client, topicArn, eventName, publicPgpKey);
	}

	private PublisherService(AmazonSNS client, String topicArn, String eventName) {
		this.amazonSNS = client;
		this.topicArn = topicArn;
		this.eventName = eventName;
		this.messageSerializer = new MessageSerializer<>();
	}

	private PublisherService(AmazonSNS client, String topicArn, String eventName, byte[] publicPgpKey) throws MessagingException {
		this.amazonSNS = client;
		this.topicArn = topicArn;
		this.eventName = eventName;
		try {
			this.messageSerializer = new MessageSerializer<>(publicPgpKey);
		} catch (CryptographyException e) {
			throw new MessagingException("Failed to load public PGP key needed to encrypt messages.", e);
		}
	}

	/**
	 * Posts message to queue.
	 *
	 * @param message Message to post.
	 * @return The message string without encryption and message id format messageid:jsonstring
	 * @throws MessagingException Failed to post message.
	 */
	@Override
	public MessageReceipt post(M message) throws MessagingException {
		try {
			String subject = getEventNameForMessage(message);
			String jsonBody = messageSerializer.serialize(message);
			String encryptedBody = messageSerializer.encrypt(jsonBody);
			PublishRequest publishRequest = new PublishRequest(topicArn, encryptedBody, subject);
			PublishResult publishResult = amazonSNS.publish(publishRequest);
			return new MessageReceipt(publishResult.getMessageId(), jsonBody);
		} catch (IOException | CryptographyException e) {
			throw new MessagingException("Failed to publish message: " + message.getClass().toString(), e);
		}
	}

	private String getEventNameForMessage(M message) {
		return coalesce(this.eventName, message.getClass().getName());
	}
}
