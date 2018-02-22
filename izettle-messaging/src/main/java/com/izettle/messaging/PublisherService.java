package com.izettle.messaging;

import static com.izettle.java.ValueChecks.anyEmpty;
import static com.izettle.java.ValueChecks.empty;
import static com.izettle.java.ValueChecks.noneNull;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.izettle.cryptography.CryptographyException;
import com.izettle.messaging.serialization.DefaultMessageSerializer;
import com.izettle.messaging.serialization.MessageSerializer;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Convenience class for using Amazon Simple Notification Service.
 */
public class PublisherService implements MessagePublisherWithAttributes {

    private final String topicArn;
    private final AmazonSNS amazonSNS;
    private final MessageSerializer messageSerializer;

    public static MessagePublisherWithAttributes nonEncryptedPublisherService(AmazonSNS client, final String topicArn) {
        return PublisherService.nonEncryptedPublisherService(client, topicArn, new DefaultMessageSerializer());
    }

    public static MessagePublisherWithAttributes nonEncryptedPublisherService(
            final AmazonSNS client,
            final String topicArn,
            final MessageSerializer messageSerializer
    ) {
        return new PublisherService(client, topicArn, messageSerializer);
    }

    public static MessagePublisher encryptedPublisherService(
            AmazonSNS client,
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
        return new PublisherService(client, topicArn, messageSerializer);
    }

    private PublisherService(AmazonSNS client, String topicArn, MessageSerializer messageSerializer) {
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
     * Posts message to queue.
     *
     * @param message Message to post.
     * @param eventName Message subject (type of message).
     * @throws MessagingException Failed to post message.
     */
    @Override
    public <M> void post(M message, String eventName) throws MessagingException {
        post(message, eventName, null);
    }

    /**
     * Posts message to queue.
     *
     * @param message Message to post.
     * @param eventName Message subject (type of message).
     * @param attributes Attributes to be set as MessageAttributes on the publishRequest
     *                   Message attributes allow you to provide structured metadata items
     *                   (such as timestamps, geospatial data, signatures, and identifiers)
     *                   about the message. Message attributes are optional and separate from,
     *                   but sent along with, the message body.
     *                   This information can be used by the consumer of the message
     *                   to help decide how to handle the message without having to first process the message body.
     *                   Each message can have up to 10 attributes.
     *                   To specify message attributes, you can use the AWS Management Console,
     *                   AWS software development kits (SDKs), or Query API.
     * @throws MessagingException Failed to post message.
     */
    @Override
    public <M> void post(M message, String eventName, Map<String, String> attributes) throws MessagingException {
        if (empty(eventName)) {
            throw new MessagingException("Cannot publish message with empty eventName!");
        }
        if (noneNull(attributes) && attributes.size() > 10) {
            throw new MessagingException("Cannot publish message with more than 10 attributes!");
        }
        try {
            String jsonBody = messageSerializer.serialize(message);
            String encryptedBody = messageSerializer.encrypt(jsonBody);
            PublishRequest publishRequest = new PublishRequest(topicArn, encryptedBody, eventName);

            if (attributes != null) {
                publishRequest.setMessageAttributes(
                    attributes
                        .entrySet()
                        .stream()
                        .collect(
                            Collectors.toMap(
                                Map.Entry::getKey,
                                e -> new MessageAttributeValue().withStringValue(e.getValue()).withDataType("String")
                            )
                        )
                );
            }
            amazonSNS.publish(publishRequest);
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
