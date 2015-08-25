package com.izettle.messaging;

import static com.izettle.java.ValueChecks.empty;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.izettle.messaging.serialization.MessageDeserializer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Convenience class for consuming messages from a single queue in Amazon Simple Queue Service.
 *
 * @param <M> Message type.
 */
public class QueueServicePoller<M> implements MessageQueueConsumer<M> {

    private static final Logger LOG = Logger.getLogger(QueueServicePoller.class.getName());

    private static final int MESSAGE_WAIT_SECONDS = 20;
    private static final boolean DEFAULT_LONG_POLLING = true;
    private static final int MAXIMUM_NUMBER_OF_MESSAGES_TO_RECEIVE = 10;
    private final String queueUrl;
    private final AmazonSQS amazonSQS;
    private final MessageDeserializer<M> messageDeserializer;
    private final boolean useLongPolling;

    public static <T> MessageQueueConsumer<T> nonEncryptedMessageQueueConsumer(
        final Class<T> messageClass,
        final String queueUrl,
        final AmazonSQS amazonSQSClient,
        boolean useLongPolling
    ) {
        return new QueueServicePoller<>(
            queueUrl,
            amazonSQSClient,
            new MessageDeserializer<>(messageClass),
            useLongPolling);
    }

    public static <T> MessageQueueConsumer<T> encryptedMessageQueueConsumer(
        final Class<T> messageClass,
        final String queueUrl,
        final AmazonSQS amazonSQSClient,
        byte[] privatePgpKey,
        final String privatePgpKeyPassphrase,
        boolean useLongPolling
    ) throws MessagingException {
        if (empty(privatePgpKey) || empty(privatePgpKeyPassphrase)) {
            throw new MessagingException(
                "Can't create encryptedQueueServicePoller with private PGP key as null or privatePgpKeyPassphrase as null");
        }
        return new QueueServicePoller<>(
            queueUrl,
            amazonSQSClient,
            new MessageDeserializer<>(messageClass, privatePgpKey, privatePgpKeyPassphrase),
            useLongPolling);
    }

    public static <T> MessageQueueConsumer<T> nonEncryptedMessageQueueConsumer(
        final Class<T> messageClass,
        final String queueUrl,
        final AmazonSQS amazonSQSClient
    ) {
        return nonEncryptedMessageQueueConsumer(
            messageClass,
            queueUrl,
            amazonSQSClient,
            DEFAULT_LONG_POLLING);
    }

    public static <T> MessageQueueConsumer<T> encryptedMessageQueueConsumer(
        final Class<T> messageClass,
        final String queueUrl,
        final AmazonSQS amazonSQSClient,
        byte[] privatePgpKey,
        final String privatePgpKeyPassphrase
    ) throws MessagingException {
        return encryptedMessageQueueConsumer(
            messageClass,
            queueUrl,
            amazonSQSClient,
            privatePgpKey,
            privatePgpKeyPassphrase,
            DEFAULT_LONG_POLLING);
    }

    private QueueServicePoller(
            String queueUrl,
            AmazonSQS amazonSQS,
            MessageDeserializer<M> messageDeserializer,
            boolean useLongPolling
    ) {
        this.queueUrl = queueUrl;
        this.amazonSQS = amazonSQS;
        this.messageDeserializer = messageDeserializer;
        this.useLongPolling = useLongPolling;
    }

    /**
     * Deletes a message from queue.
     *
     * @param message Received message.
     * @throws MessagingException Failed to delete message.
     */
    @Override
    public void delete(PolledMessage<M> message) throws MessagingException {
        try {
            amazonSQS.deleteMessage(new DeleteMessageRequest(queueUrl, message.getMessageId()));
        } catch (AmazonClientException ase) {
            throw new MessagingException("Failed to delete message with id " + message.getMessageId(), ase);
        }
    }

    /**
     * Polls message queue for new messages.
     *
     * Waits for messages for 20 seconds if long polling is enabled.
     *
     * @return Received messages.
     * @throws MessagingException Failed to poll queue.
     */
    @Override
    public List<PolledMessage<M>> poll() throws MessagingException {
        return poll(MESSAGE_WAIT_SECONDS);
    }


    /**
     * Polls message queue for new messages.
     *
     * @param messageWaitTimeInSeconds
     *  nr of seconds the poll should wait if long polling is enabled.
     * @return Received messages.
     * @throws MessagingException Failed to poll queue.
     */
    @Override
    public List<PolledMessage<M>> poll(int messageWaitTimeInSeconds) throws MessagingException {
        ReceiveMessageRequest messageRequest = new ReceiveMessageRequest(queueUrl);
        messageRequest.setMaxNumberOfMessages(MAXIMUM_NUMBER_OF_MESSAGES_TO_RECEIVE);
        if (useLongPolling) {
            messageRequest.setWaitTimeSeconds(messageWaitTimeInSeconds);
        }
        List<Message> messages;
        List<PolledMessage<M>> receivedMessages = new ArrayList<>();

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
                PolledMessage<M> receivedMessage = new PolledMessage<>(messageEntity, messageReceiptHandle);
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
