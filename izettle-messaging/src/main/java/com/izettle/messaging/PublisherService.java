package com.izettle.messaging;

import static com.izettle.java.ValueChecks.isEmpty;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.izettle.messaging.serialization.MessageSerializer;
import java.io.IOException;
import java.io.InputStream;
import org.bouncycastle.openpgp.PGPException;

/**
 * Convenience class for using Amazon Simple Notification Service.
 *
 * @param <M> Message type.
 */
public class PublisherService<M> implements MessageQueueProducer<M> {
	private final String topicArn;
	private final AmazonSNS amazonSNS;
	private final MessageSerializer<M> messageSerializer;

	public static <T> MessageQueueProducer<T> nonEncryptedPublisherService(final String topicArn, final String accessKey, final String secretKey, final String endpoint) throws MessagingException {
		return new PublisherService<>(topicArn, accessKey, secretKey, endpoint, null);
	}

	public static <T> MessageQueueProducer<T> nonEncryptedPublisherService(final String topicArn, InputStream credentialsInputStream, final String endpoint) throws MessagingException {
		return new PublisherService<>(topicArn, AWSCredentialsWrapper.getCredentials(credentialsInputStream), endpoint, null);
	}

	public static <T> MessageQueueProducer<T> encryptedPublisherService(final String topicArn, final String accessKey, final String secretKey, final String endpoint, final byte[] publicPgpKey) throws MessagingException {
		if (isEmpty(publicPgpKey)) {
			throw new MessagingException("Can't create encryptedPublisherService with null as public PGP key");
		}
		return new PublisherService<>(topicArn, accessKey, secretKey, endpoint, publicPgpKey);
	}

	private PublisherService(final String topicArn, final String accessKey, final String secretKey, final String endpoint, final byte[] publicPgpKey) throws MessagingException {
		this(topicArn, AWSCredentialsWrapper.getCredentials(accessKey, secretKey), endpoint, publicPgpKey);
	}

	private PublisherService(String topicArn, AWSCredentials awsCredentials, String endpoint, byte[] publicPgpKey) throws MessagingException {
		try {
			this.topicArn = topicArn;
			this.amazonSNS = AmazonSNSClientFactory.getInstance(endpoint, awsCredentials);
			this.messageSerializer = new MessageSerializer<M>(publicPgpKey);
		} catch (PGPException e) {
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
		} catch (IOException | PGPException e) {
			throw new MessagingException("Failed to publish message: " + message.getClass().toString(), e);
		}
	}
}
