package com.izettle.messaging;

import static com.izettle.java.CollectionUtils.partition;
import static com.izettle.java.ValueChecks.empty;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.izettle.cryptography.CryptographyException;
import com.izettle.cryptography.HashMD5;
import com.izettle.messaging.serialization.MessageDeserializer;
import com.izettle.messaging.serialization.MessageSerializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convenience class for using Amazon Simple Queue Service.
 *
 * @param <M> Message type.
 */
public class QueueService<M> implements MessageQueueProducer<M>, MessageQueueConsumer<M> {

	private static final Logger LOG = Logger.getLogger(QueueService.class.getName());
	private static final int MAXIMUM_NUMBER_OF_MESSAGES_TO_RECEIVE = 10;
	private static final int MAX_BATCH_SIZE = 10;
	private final String queueUrl;
	private final AmazonSQS amazonSQS;
	private final MessageSerializer<M> messageSerializer;
	private final MessageDeserializer<M> messageDeserializer;

	public static <T> QueueService<T> nonEncryptedQueueService(
			final Class<T> messageClass,
			final String queueUrl,
			final AmazonSQS amazonSQSClient
	) {
		return new QueueService<>(messageClass,
				queueUrl,
				amazonSQSClient);
	}

	public static <T> MessageQueueConsumer<T> encryptedQueueServicePoller(
			final Class<T> messageClass,
			final String queueUrl,
			final AmazonSQS amazonSQSClient,
			byte[] privatePgpKey,
			final String privatePgpKeyPassphrase
	) throws MessagingException {
		if (empty(privatePgpKey) || empty(privatePgpKeyPassphrase)) {
			throw new MessagingException("Can't create encryptedQueueServicePoller with private PGP key as null or privatePgpKeyPassphrase as null");
		}
		return new QueueService<>(messageClass,
				queueUrl,
				amazonSQSClient,
				privatePgpKey,
				privatePgpKeyPassphrase);
	}

	public static <T> MessageQueueProducer<T> encryptedQueueServicePoster(
			final Class<T> messageClass,
			final String queueUrl,
			final AmazonSQS amazonSQSClient,
			final byte[] publicPgpKey
	) throws MessagingException {
		if (empty(publicPgpKey)) {
			throw new MessagingException("Can't create encryptedQueueServicePoster with null as public PGP key");
		}
		return new QueueService<>(messageClass,
				queueUrl,
				amazonSQSClient,
				publicPgpKey);
	}

	QueueService(
			Class<M> messageClass,
			String queueUrl,
			AmazonSQS amazonSQS,
			byte[] privatePgpKey,
			String privatePgpKeyPassphrase
	) {
		this.queueUrl = queueUrl;
		this.amazonSQS = amazonSQS;
		this.messageSerializer = new MessageSerializer<>();
		this.messageDeserializer = new MessageDeserializer<>(messageClass, privatePgpKey, privatePgpKeyPassphrase);
	}

	QueueService(
			Class<M> messageClass,
			String queueUrl,
			AmazonSQS amazonSQS,
			byte[] publicPgpKey
	) throws MessagingException {
			this.queueUrl = queueUrl;
			this.amazonSQS = amazonSQS;
		try {
			this.messageSerializer = new MessageSerializer<>(publicPgpKey);
		} catch (CryptographyException e) {
			throw new MessagingException("Failed to load public PGP key needed to encrypt messages.", e);
		}
		this.messageDeserializer = new MessageDeserializer<>(messageClass);
	}

	QueueService(
			Class<M> messageClass,
			String queueUrl,
			AmazonSQS amazonSQS
	) {
		this.queueUrl = queueUrl;
		this.amazonSQS = amazonSQS;
		this.messageSerializer = new MessageSerializer<>();
		this.messageDeserializer = new MessageDeserializer<>(messageClass);
	}

	/**
	 * Deletes a message from queue.
	 *
	 * @param messageWrapper Received message.
	 * @throws MessagingException Failed to delete message.
	 */
	@Override
	public void delete(MessageWrapper<M> messageWrapper) throws MessagingException {
		try {
			amazonSQS.deleteMessage(new DeleteMessageRequest(queueUrl, messageWrapper.getMessageReceiptHandle()));
		} catch (AmazonClientException ase) {
			throw new MessagingException("Failed to delete message with receipt handle " + messageWrapper.getMessageReceiptHandle(), ase);
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
			String jsonBody = messageSerializer.serialize(message);
			String encryptedBody = messageSerializer.encrypt(jsonBody);
			SendMessageResult sendMessageResult = amazonSQS.sendMessage(new SendMessageRequest(queueUrl, encryptedBody));
			return new MessageReceipt(sendMessageResult.getMessageId(), jsonBody);
		} catch (IOException | CryptographyException e) {
			throw new MessagingException("Failed to post message: " + message.getClass().toString(), e);
		}
	}

	/**
	 * Posts many messages to queue
	 *
	 * @param messages a map of messages to post, each with a unique id within the request
	 * @throws MessagingException Failed to post messages.
	 */
	public void postBatch(Map<String, M> messages) throws MessagingException {
		try {
			for (Collection<SendMessageBatchRequestEntry> sendMessageBatchRequest : partition(prepareBatchEntries(messages), MAX_BATCH_SIZE)) {
				amazonSQS.sendMessageBatch(new SendMessageBatchRequest(queueUrl, new ArrayList<>(sendMessageBatchRequest)));

			}
		} catch (IOException | CryptographyException e) {
			throw new MessagingException("Failed to post messages: " + messages.getClass().toString(), e);
		}
	}

	private List<SendMessageBatchRequestEntry> prepareBatchEntries(Map<String, M> messages) throws IOException, CryptographyException {
		List<SendMessageBatchRequestEntry> batchRequestEntries = new ArrayList<>(messages.size());

		for (Entry<String, M> messageEntry : messages.entrySet()) {
			batchRequestEntries.add(new SendMessageBatchRequestEntry(messageEntry.getKey(),
					messageSerializer.encrypt(messageSerializer.serialize(messageEntry.getValue()))));
		}

		return batchRequestEntries;
	}
	/**
	 * Polls message queue for new messages. Waits for messages for 20 sek.
	 *
	 * @return Received messages.
	 * @throws MessagingException Failed to poll queue.
	 */
	@Override
	public List<MessageWrapper<M>> poll() throws MessagingException {
		return poll(20);
	}


	/**
	 * Polls message queue for new messages.
	 *
	 * @param messageWaitTimeInSeconds, nr of seconds the poll should wait.
	 * @return Received messages.
	 * @throws MessagingException Failed to poll queue.
	 */
	@Override
	public List<MessageWrapper<M>> poll(int messageWaitTimeInSeconds) throws MessagingException {
		ReceiveMessageRequest messageRequest = new ReceiveMessageRequest(queueUrl);
		messageRequest.setMaxNumberOfMessages(MAXIMUM_NUMBER_OF_MESSAGES_TO_RECEIVE);
		messageRequest.setWaitTimeSeconds(messageWaitTimeInSeconds);
		List<Message> messages;
		List<MessageWrapper<M>> receivedMessages = new ArrayList<>();

		try {
			messages = amazonSQS.receiveMessage(messageRequest).getMessages();
		} catch (AmazonClientException e) {
			throw new MessagingException("Failed to poll message queue.", e);
		}

		for (Message message : messages) {
			String decryptedMessage = "";
			try {
				String messageBody = message.getBody();
				messageBody = MessageDeserializer.removeSnsEnvelope(messageBody);
				decryptedMessage = messageDeserializer.decrypt(messageBody);
				M messageEntity = messageDeserializer.deserialize(decryptedMessage);
				String messageReceiptHandle = message.getReceiptHandle();
				MessageWrapper<M> receivedMessage = new MessageWrapper<>(messageEntity, messageReceiptHandle, HashMD5.digestStringsToB64Hash(decryptedMessage), message.getMessageId());
				receivedMessages.add(receivedMessage);
			} catch (Exception e) {
				/*
				 Note: We should only log here and continue with the other messages fetched. The reason for that is
				 that we can during release have different versions of the messages, some possible to parse and some
				 not.
				 */
				LOG.log(Level.WARNING, "Failed to read message " + message.getMessageId() + " from queue. \n DecryptedMessage: \n" + decryptedMessage, e);
			}
		}
		return receivedMessages;
	}
}
