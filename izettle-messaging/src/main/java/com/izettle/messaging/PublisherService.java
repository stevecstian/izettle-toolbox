package com.izettle.messaging;

import static com.izettle.java.ValueChecks.isEmpty;

import com.amazonaws.services.sns.AmazonSNSClient;
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
	private final AmazonSNSClient amazonSNS;
	private final MessageSerializer<M> messageSerializer;

	public static <T> MessageQueueProducer<T> nonEncryptedPublisherService(AmazonSNSClient client, final String topicArn) {
		return new PublisherService<>(client, topicArn);
	}

	public static <T> MessageQueueProducer<T> encryptedPublisherService(AmazonSNSClient client, final String topicArn, final byte[] publicPgpKey) throws MessagingException {
		if (isEmpty(publicPgpKey)) {
			throw new MessagingException("Can't create encryptedPublisherService with null as public PGP key");
		}
		return new PublisherService<>(client, topicArn, publicPgpKey);
	}

	private PublisherService(AmazonSNSClient client, String topicArn) {
		this.amazonSNS = client;
		this.topicArn = topicArn;
		this.messageSerializer = new MessageSerializer<>();
	}

	private PublisherService(AmazonSNSClient client, String topicArn, byte[] publicPgpKey) throws MessagingException {
		this.amazonSNS = client;
		this.topicArn = topicArn;
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
			String subject = message.getClass().getName();
			String jsonBody = messageSerializer.serialize(message);
			String encryptedBody = messageSerializer.encrypt(jsonBody);
			PublishRequest publishRequest = new PublishRequest(topicArn, encryptedBody, subject);
			PublishResult publishResult = amazonSNS.publish(publishRequest);
			return new MessageReceipt(publishResult.getMessageId(), jsonBody);
		} catch (IOException | CryptographyException e) {
			throw new MessagingException("Failed to publish message: " + message.getClass().toString(), e);
		}
	}
}
