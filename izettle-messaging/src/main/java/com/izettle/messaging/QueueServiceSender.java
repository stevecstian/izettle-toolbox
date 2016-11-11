package com.izettle.messaging;

import static com.izettle.java.CollectionUtils.partition;
import static com.izettle.java.ValueChecks.anyEmpty;
import static com.izettle.java.ValueChecks.empty;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.BatchResultErrorEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izettle.cryptography.CryptographyException;
import com.izettle.messaging.serialization.AmazonSNSMessage;
import com.izettle.messaging.serialization.DefaultMessageSerializer;
import com.izettle.messaging.serialization.JsonSerializer;
import com.izettle.messaging.serialization.MessageSerializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class for sending messages to a single queue in Amazon Simple Queue Service.
 *
 * @param <M> Message type.
 */
public class QueueServiceSender<M> implements MessageQueueProducer<M>, MessagePublisher {
    private static final Logger LOG = LoggerFactory.getLogger(QueueServiceSender.class);
    private static final int MAX_BATCH_SIZE = 10;
    private final String queueUrl;
    private final AmazonSQS amazonSQS;
    private final MessageSerializer messageSerializer;
    private final ObjectMapper jsonMapper = JsonSerializer.getInstance();

    public static MessagePublisher nonEncryptedMessagePublisher(
            final String queueUrl,
            final AmazonSQS amazonSQSClient
    ) {
        return new QueueServiceSender<>(queueUrl, amazonSQSClient, new DefaultMessageSerializer());
    }

    public static <T> MessageQueueProducer<T> nonEncryptedMessageQueueProducer(
            final String queueUrl,
            final AmazonSQS amazonSQSClient
    ) {
        return new QueueServiceSender<>(queueUrl, amazonSQSClient, new DefaultMessageSerializer());
    }

    public static MessagePublisher nonEncryptedMessagePublisher(
            final String queueUrl,
            final AmazonSQS amazonSQSClient,
            final MessageSerializer messageSerializer
    ) {
        return new QueueServiceSender<>(queueUrl, amazonSQSClient, messageSerializer);
    }

    public static MessagePublisher encryptedMessagePublisher(
        final String queueUrl,
        final AmazonSQS amazonSQSClient,
        final byte[] publicPgpKey
    ) throws MessagingException {
        MessageSerializer messageSerializer = createEncryptedMessageSerializer(publicPgpKey);
        return new QueueServiceSender<>(queueUrl, amazonSQSClient, messageSerializer);
    }

    public static <T> MessageQueueProducer<T> nonEncryptedMessageQueueProducer(
            final String queueUrl,
            final AmazonSQS amazonSQSClient,
            final MessageSerializer messageSerializer
    ) {
        return new QueueServiceSender<>(queueUrl, amazonSQSClient, messageSerializer);
    }

    public static <T> MessageQueueProducer<T> encryptedMessageQueueProducer(
            final String queueUrl,
            final AmazonSQS amazonSQSClient,
            final byte[] publicPgpKey
    ) throws MessagingException {
        MessageSerializer messageSerializer = createEncryptedMessageSerializer(publicPgpKey);
        return new QueueServiceSender<>(queueUrl, amazonSQSClient, messageSerializer);
    }

    private static MessageSerializer createEncryptedMessageSerializer(byte[] publicPgpKey) throws MessagingException {
        if (empty(publicPgpKey)) {
            throw new MessagingException("Can't create encryptedQueueServicePoster with null as public PGP key");
        }
        try {
            return new DefaultMessageSerializer(publicPgpKey);
        } catch (CryptographyException e) {
            throw new MessagingException("Failed to load public PGP key needed to encrypt messages.", e);
        }
    }

    private QueueServiceSender(
            String queueUrl,
            AmazonSQS amazonSQS,
            MessageSerializer messageSerializer
    ) {
        if (anyEmpty(queueUrl, amazonSQS, messageSerializer)) {
            throw new IllegalArgumentException(
                    "None of queueUrl, amazonSQS or messageSerializer can be empty!\n"
                            + "queueUrl = " + queueUrl + "\n"
                            + "amazonSQS = " + amazonSQS + "\n"
                            + "messageSerializer = " + messageSerializer
            );
        }
        this.queueUrl = queueUrl;
        this.amazonSQS = amazonSQS;
        this.messageSerializer = messageSerializer;
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
            SendMessageResult sendMessageResult = amazonSQS.sendMessage(
                    new SendMessageRequest(queueUrl, encryptedBody)
            );
            return new MessageReceipt(sendMessageResult.getMessageId(), jsonBody);
        } catch (AmazonServiceException | IOException | CryptographyException e) {
            throw new MessagingException("Failed to post message: " + message.getClass(), e);
        }
    }

    /**
     * Posts a single messages to queue, with a message envelope that makes it look like it
     * was sent through Amazon SNS.
     *
     * @param message message to post
     * @param eventName the value that will be used as "subject" in the SNS envelope
     * @throws MessagingException Failed to post message.
     */
    @Override
    public <T> void post(T message, String eventName) throws MessagingException {
        if (empty(eventName)) {
            throw new MessagingException("Cannot publish message with empty eventName!");
        }
        try {
            amazonSQS.sendMessage(
                new SendMessageRequest(queueUrl, wrapInSNSMessage(message, eventName))
            );
        } catch (Exception e) {
            throw new MessagingException("Failed to post message: " + message.getClass(), e);
        }
    }

    /**
     * Posts many messages to queue, with a message envelope that makes them look like they
     * were sent through Amazon SNS.
     *
     * @param messages list of messages to post
     * @param eventName the value that will be used as "subject" in the SNS envelope
     * @throws MessagingException Failed to post messages.
     */
    @Override
    public <T> void postBatch(Collection<T> messages, String eventName) throws MessagingException {
        if (empty(eventName)) {
            throw new MessagingException("Cannot publish message with empty eventName!");
        }
        try {
            Collection<SendMessageBatchRequestEntry> allEntries = new ArrayList<>(messages.size());
            int messageIdInBatch = 0;
            for (T message : messages) {
                ++messageIdInBatch;
                String messageBody = wrapInSNSMessage(message, eventName);
                allEntries.add(new SendMessageBatchRequestEntry(String.valueOf(messageIdInBatch), messageBody));
            }
            sendMessageBatch(allEntries);
        } catch (AmazonServiceException | IOException | CryptographyException e) {
            throw new MessagingException("Failed to post messages: " + messages.getClass(), e);
        }
    }

    private String wrapInSNSMessage(
            Object message,
            String subject
    ) throws JsonProcessingException, CryptographyException {
        String messageBody = messageSerializer.encrypt(messageSerializer.serialize(message));
        AmazonSNSMessage snsMessage = new AmazonSNSMessage(subject, messageBody);
        return jsonMapper.writeValueAsString(snsMessage);
    }

    private void sendMessageBatch(Collection<SendMessageBatchRequestEntry> messages) {
        for (Collection<SendMessageBatchRequestEntry> batch : partition(messages, MAX_BATCH_SIZE)) {
            final SendMessageBatchResult sendMessageBatchResult =
                amazonSQS.sendMessageBatch(new SendMessageBatchRequest(queueUrl, new ArrayList<>(batch)));
            final List<BatchResultErrorEntry> failed = sendMessageBatchResult.getFailed();
            if (!failed.isEmpty()) {
                try {
                    Set<String> failedMessageIds =
                        failed.stream().map(BatchResultErrorEntry::getId).collect(Collectors.toSet());
                    final Map<String, SendMessageBatchRequestEntry> failedMessageIdToMessage =
                        batch.stream().filter(failedMessageIds::contains).collect(Collectors.toMap(
                            SendMessageBatchRequestEntry::getId,
                            Function.identity()
                        ));
                    failed.stream().forEach(failMessage -> {
                        final SendMessageBatchRequestEntry failedEntry =
                            failedMessageIdToMessage.get(failMessage.getId());
                        if (failedEntry != null) {
                            final String messageBody = failedEntry.getMessageBody();
                            LOG.error(
                                "Failed to send message, due to {}, message content : {} ",
                                failMessage,
                                messageBody
                            );
                        }
                    });
                } catch (Exception e) {
                    LOG.error("Failed to log failed to send messages", e);
                }
            }
        }
    }
}
