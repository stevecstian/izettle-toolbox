package com.izettle.messaging;

import static com.izettle.java.ValueChecks.empty;

import com.amazonaws.AbortedException;
import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.izettle.messaging.handler.AsyncMessageDispatcher;
import com.izettle.messaging.handler.MessageHandler;
import com.izettle.messaging.handler.MessageHandlerForSingleMessageType;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a poller on a single queue. All messages that get received on the queue
 * will be passed on to the supplied message handler. If the message handler does not throw any
 * exceptions, the message will also be deleted from the queue. Otherwise (if an exception is
 * thrown), the message will remain on the queue, and will most likely be processed again.
 */
public class QueueProcessor implements MessageQueueProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(QueueProcessor.class);
    private final String name;

    private static final int MAXIMUM_NUMBER_OF_MESSAGES_TO_RECEIVE = 10;
    private static final Integer MESSAGE_WAIT_SECONDS = 20;
    private static final int DEAD_LETTER_QUEUE_POLL_FREQUENCY = 10;
    private final String queueUrl;
    private final String deadLetterQueueUrl;
    private final AmazonSQS amazonSQS;
    private final MessageHandler<Message> messageHandler;
    private final AsyncMessageDispatcher asyncMessageDispatcher;
    private int deadLetterQueuePollSequence;
    private ExecutorService executorService;

    public static MessageQueueProcessor createQueueProcessor(
        AmazonSQS amazonSQS,
        String name,
        String queueUrl,
        String deadLetterQueueUrl,
        MessageHandler<Message> messageHandler
    ) {
        return new QueueProcessor(
            name,
            queueUrl,
            deadLetterQueueUrl,
            amazonSQS,
            messageHandler,
            null
        );
    }

    public static MessageQueueProcessor createQueueProcessor(
        AmazonSQS amazonSQS,
        String name,
        String queueUrl,
        String deadLetterQueueUrl,
        AsyncMessageDispatcher asyncMessageDispatcher
    ) {
        return new QueueProcessor(
            name,
            queueUrl,
            deadLetterQueueUrl,
            amazonSQS,
            asyncMessageDispatcher,
            null
        );
    }

    public static MessageQueueProcessor createQueueProcessor(
        AmazonSQS amazonSQS,
        String name,
        String queueUrl,
        String deadLetterQueueUrl,
        MessageHandler<Message> messageHandler,
        ExecutorService executorService
    ) {
        return new QueueProcessor(
            name,
            queueUrl,
            deadLetterQueueUrl,
            amazonSQS,
            messageHandler,
            executorService
        );
    }

    public static <M> MessageQueueProcessor createQueueProcessor(
        AmazonSQS amazonSQS,
        Class<M> classType,
        String name,
        String queueUrl,
        String deadLetterQueueUrl,
        MessageHandler<M> messageHandler
    ) {
        return new QueueProcessor(
            name,
            queueUrl,
            deadLetterQueueUrl,
            amazonSQS,
            new MessageHandlerForSingleMessageType<>(messageHandler, classType),
            null
        );
    }

    public static <M> MessageQueueProcessor createQueueProcessor(
        AmazonSQS amazonSQS,
        Class<M> classType,
        String name,
        String queueUrl,
        String deadLetterQueueUrl,
        MessageHandler<M> messageHandler,
        ExecutorService executorService
    ) {
        return new QueueProcessor(
            name,
            queueUrl,
            deadLetterQueueUrl,
            amazonSQS,
            new MessageHandlerForSingleMessageType<>(messageHandler, classType),
            executorService
        );
    }

    private QueueProcessor(
        String name,
        String queueUrl,
        String deadLetterQueueUrl,
        AmazonSQS amazonSQS,
        MessageHandler<Message> messageHandler,
        ExecutorService executorService
    ) {
        this.name = name;
        this.queueUrl = queueUrl;
        this.deadLetterQueueUrl = deadLetterQueueUrl;
        this.amazonSQS = amazonSQS;
        this.messageHandler = messageHandler;
        this.asyncMessageDispatcher = null;
        this.executorService = executorService;
    }

    private QueueProcessor(
        String name,
        String queueUrl,
        String deadLetterQueueUrl,
        AmazonSQS amazonSQS,
        AsyncMessageDispatcher asyncMessageDispatcher,
        ExecutorService executorService
    ) {
        this.name = name;
        this.queueUrl = queueUrl;
        this.deadLetterQueueUrl = deadLetterQueueUrl;
        this.amazonSQS = amazonSQS;
        this.messageHandler = null;
        this.asyncMessageDispatcher = asyncMessageDispatcher;
        this.executorService = executorService;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void poll() throws MessagingException {
        pollMessageQueue(queueUrl, true);

        /*
            Poll the dead letter queue (if specified) every DEAD_LETTER_QUEUE_POLL_FREQUENCY:th poll attempt.
         */
        if (!empty(deadLetterQueueUrl)) {
            deadLetterQueuePollSequence = (deadLetterQueuePollSequence + 1) % DEAD_LETTER_QUEUE_POLL_FREQUENCY;
            if (deadLetterQueuePollSequence == 0) {
                pollMessageQueue(deadLetterQueueUrl, false);
            }
        }
    }

    private void pollMessageQueue(String messageQueueUrl, boolean useLongPolling) throws MessagingException {
        ReceiveMessageRequest messageRequest = new ReceiveMessageRequest(messageQueueUrl);
        messageRequest.setMaxNumberOfMessages(MAXIMUM_NUMBER_OF_MESSAGES_TO_RECEIVE);
        if (useLongPolling) {
            messageRequest.setWaitTimeSeconds(MESSAGE_WAIT_SECONDS);
        }
        try {
            List<Message> messages = amazonSQS.receiveMessage(messageRequest).getMessages();
            if (!empty(messages)) {
                if (executorService != null) {
                    try {
                        executorService.submit(() -> handleMessages(messages, messageQueueUrl));
                    } catch (RejectedExecutionException e) {
                        LOG.warn("Use executorService but been rejected!", e);
                        handleMessages(messages, messageQueueUrl);
                    }
                } else {
                    handleMessages(messages, messageQueueUrl);
                }
            }
        } catch (AbortedException e) {
            LOG.info("Client abort receive message.");
        } catch (AmazonClientException e) {
            throw new MessagingException("Failed to poll message queue.", e);
        }
    }

    private void handleMessages(List<Message> messages, String messageQueueUrl) {
        LOG.debug("Message queue processor {} fetched {} message(s) from queue.", name, messages.size());

        for (Message message : messages) {
            try {
                if (null == asyncMessageDispatcher) {
                    messageHandler.handle(message);
                    deleteMessageFromQueue(message.getReceiptHandle(), messageQueueUrl);
                } else {
                    asyncMessageDispatcher.handle(message);
                }
            } catch (RetryableMessageHandlerException ignored) {
                /*
                 If the message handler throws this exception, we should retry handling the message some time later.
                 This could be the case where the handler is waiting for other messages to come in first, before
                 handling this particular message.
                 Regardless, the handler has decided that this is not a general error situation, and thus
                 should not be logged in the same way that general exceptions (below) are done.
                 The message will be polled again by Amazon SQS.
                 */
                LOG.debug("Will retry handling message {} later.", message.getMessageId());
            } catch (Exception e) {
                /*
                 Note: We should only log here and continue with the other messages fetched. The reason for that is
                 that we can during release have different versions of the messages, some possible to parse and some
                 not.
                 Please note that in Amazon SQS, the message will be retried after some time (default 30s).
                 */
                LOG.warn("Failed to handle message {} from queue {}. Will leave it on queue.", message.getMessageId(), messageQueueUrl, e);
            }
        }
    }

    private void deleteMessageFromQueue(String messageReceiptHandle, String messageQueueUrl) throws MessagingException {
        try {
            amazonSQS.deleteMessage(new DeleteMessageRequest(messageQueueUrl, messageReceiptHandle));
        } catch (AbortedException e) {
            LOG.info("Client abort delete message.");
        } catch (AmazonClientException ase) {
            throw new MessagingException("Failed to delete message with receipt handle " + messageReceiptHandle + " from queue " + messageQueueUrl, ase);
        }
    }
}
