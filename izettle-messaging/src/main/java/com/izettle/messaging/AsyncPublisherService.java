package com.izettle.messaging;

import static com.izettle.java.ValueChecks.anyEmpty;
import static com.izettle.java.ValueChecks.empty;

import com.amazonaws.handlers.AsyncHandler;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.izettle.cryptography.CryptographyException;
import com.izettle.messaging.serialization.DefaultMessageSerializer;
import com.izettle.messaging.serialization.MessageSerializer;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience class for using Amazon Simple Notification Service in an non-blocking manner.
 */
public class AsyncPublisherService implements MessagePublisher {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncPublisherService.class);

    private final String topicArn;
    private final AmazonSNSAsync amazonSNS;
    private final MessageSerializer messageSerializer;

    public static MessagePublisher nonEncryptedPublisherService(AmazonSNSAsync client, final String topicArn) {
        return AsyncPublisherService.nonEncryptedPublisherService(client, topicArn, new DefaultMessageSerializer());
    }

    public static MessagePublisher nonEncryptedPublisherService(
            final AmazonSNSAsync client,
            final String topicArn,
            final MessageSerializer messageSerializer
    ) {
        return new AsyncPublisherService(client, topicArn, messageSerializer);
    }

    public static MessagePublisher encryptedPublisherService(
            AmazonSNSAsync client,
            final String topicArn,
            final byte[] publicPgpKey
    ) throws MessagingException {

        if (empty(publicPgpKey)) {
            throw new MessagingException("Can't create encryptedPublisherService with null as public PGP key");
        }

        MessageSerializer messageSerializer;
        try {
            messageSerializer = new DefaultMessageSerializer(publicPgpKey);
        } catch (CryptographyException e) {
            throw new MessagingException("Failed to load public PGP key needed to encrypt messages.", e);
        }
        return new AsyncPublisherService(client, topicArn, messageSerializer);
    }

    private AsyncPublisherService(AmazonSNSAsync client, String topicArn, MessageSerializer messageSerializer) {
        if (anyEmpty(client, topicArn, messageSerializer)) {
            throw new IllegalArgumentException(
                    "None of client, topicArn or messageSerializer can be empty!\n"
                            + "client = " + client + "\n"
                            + "topicArn = " + topicArn + "\n"
                            + "messageSerializer = " + messageSerializer
            );
        }
        this.amazonSNS = client;
        this.topicArn = topicArn;
        this.messageSerializer = messageSerializer;
    }

    /**
     * Posts message to queue asynchronously. SNS errors will be logged.
     *
     * @param message Message to post.
     * @param eventName Message subject (type of message).
     * @throws MessagingException Failed to post message.
     */
    public <M> void post(M message, String eventName) throws MessagingException {

        if (empty(eventName)) {
            throw new MessagingException("Cannot publish message with empty eventName!");
        }
        try {
            String jsonBody = messageSerializer.serialize(message);
            String encryptedBody = messageSerializer.encrypt(jsonBody);
            PublishRequest publishRequest = new PublishRequest(topicArn, encryptedBody, eventName);
            amazonSNS.publishAsync(
                publishRequest,
                new AsyncHandler<PublishRequest, PublishResult>() {
                    @Override
                    public void onError(Exception e) {
                        LOG.warn("Failed to publish message " + eventName, e);
                    }

                    @Override
                    public void onSuccess(PublishRequest request, PublishResult publishResult) {
                        // Do nothing
                    }
                }
            );
        } catch (Exception e) {
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
