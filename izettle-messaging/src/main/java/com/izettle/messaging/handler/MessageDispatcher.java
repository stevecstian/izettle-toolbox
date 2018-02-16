package com.izettle.messaging.handler;

import static com.izettle.java.ValueChecks.empty;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izettle.messaging.MessagingException;
import com.izettle.messaging.serialization.AmazonSNSMessage;
import com.izettle.messaging.serialization.JsonSerializer;
import com.izettle.messaging.serialization.MessageDeserializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Routes messages to other MessageHandler&lt;&gt;:s based on message type. All handlers added with the
 * addHandler() method will be called whenever a message of the supplied type is received by this class.
 *
 * Note that the current mechanism for routing messages assumes that the received messages have gone
 * through Amazon SNS and that they have the message type supplied in the "Subject" field of the
 * SQS message.
 */
public class MessageDispatcher implements MessageHandler<Message> {
    private final MessageDeserializer<String> messageDeserializer;
    private final Map<String, ListOfMessageHandlersForType> messageHandlersPerEventName = new ConcurrentHashMap<>();
    private final List<MessageHandler<AmazonSNSMessage>> defaultMessageHandlers = new ArrayList<>();
    private final ObjectMapper objectMapper;

    public static MessageDispatcher nonEncryptedMessageDispatcher() {
        return nonEncryptedMessageDispatcher(JsonSerializer.getInstance());
    }

    public static MessageDispatcher nonEncryptedMessageDispatcher(ObjectMapper objectMapper) {
        return new MessageDispatcher(objectMapper);
    }

    public static MessageDispatcher encryptedMessageDispatcher(byte[] privatePgpKey, final String privatePgpKeyPassphrase) throws MessagingException {
        return encryptedMessageDispatcher(privatePgpKey, privatePgpKeyPassphrase, JsonSerializer.getInstance());
    }

    public static MessageDispatcher encryptedMessageDispatcher(
        byte[] privatePgpKey,
        final String privatePgpKeyPassphrase,
        ObjectMapper objectMapper
    ) throws MessagingException {
        if (empty(privatePgpKey) || empty(privatePgpKeyPassphrase)) {
            throw new MessagingException("Can't create encryptedMessageDispatcher with private PGP key as null or privatePgpKeyPassphrase as null");
        }
        return new MessageDispatcher(privatePgpKey, privatePgpKeyPassphrase, objectMapper);
    }


    private MessageDispatcher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.messageDeserializer = new MessageDeserializer<>(String.class, objectMapper);
    }

    private MessageDispatcher(
        byte[] privatePgpKey,
        String privatePgpKeyPassphrase,
        ObjectMapper objectMapper
    ) {
        this.objectMapper = objectMapper;
        this.messageDeserializer = new MessageDeserializer<>(
            String.class,
            privatePgpKey,
            privatePgpKeyPassphrase,
            this.objectMapper
        );
    }

    private class ListOfMessageHandlersForType<M> {
        private final Class<M> messageType;
        public final List<MessageHandler<M>> handlers = new ArrayList<>();

        ListOfMessageHandlersForType(Class<M> messageType) {
            this.messageType = messageType;
        }
        public void add(MessageHandler<M> handler) {
            handlers.add(handler);
        }
        public void callAllHandlers(String message) throws Exception {
            M msg = objectMapper.readValue(message, messageType);
            MessageDispatcher.callAllHandlers(handlers, msg);
        }
    }

    private static <M> void callAllHandlers(List<MessageHandler<M>> handlers, M message) throws Exception {
        for (MessageHandler<M> handler : handlers) {
            handler.handle(message);
        }
    }

    public <M> void addHandler(Class<M> classType, String eventName, MessageHandler<M> handler) {
        if (!messageHandlersPerEventName.containsKey(eventName)) {
            messageHandlersPerEventName.put(eventName, new ListOfMessageHandlersForType<>(classType));
        }

        @SuppressWarnings("unchecked")
        ListOfMessageHandlersForType<M> listOfMessageHandlersForType = (ListOfMessageHandlersForType<M>) messageHandlersPerEventName.get(eventName);

        listOfMessageHandlersForType.add(handler);
    }

    public <M> void addHandler(Class<M> classType, MessageHandler<M> handler) {
        addHandler(classType, classType.getName(), handler);
    }

    /*
     * Adds a message handler that should be called if none of the other message handlers match the incoming message.
     */
    public void addDefaultHandler(MessageHandler<AmazonSNSMessage> handler) {
        defaultMessageHandlers.add(handler);
    }

    @Override
    public void handle(Message message) throws Exception {
        String messageBody = message.getBody();
        AmazonSNSMessage sns = objectMapper.readValue(messageBody, AmazonSNSMessage.class);
        String decryptedMessage = messageDeserializer.decrypt(sns.getMessage());
        String eventName = sns.getSubject();
        String typeName = sns.getType();
        if (empty(eventName) && empty(typeName)) {
            throw new MessagingException(
                    "Received message without event name or type. MessageDispatcher requires a message subject or type "
                    + "in order to know which handler to route the message to. Make sure that you publish your message "
                    + "with a message subject or type before trying to receive it."
            );
        }
        if (!empty(eventName) && messageHandlersPerEventName.containsKey(eventName)) {
            messageHandlersPerEventName.get(eventName).callAllHandlers(decryptedMessage);
        } else if (!empty(typeName) && messageHandlersPerEventName.containsKey(typeName)) {
            messageHandlersPerEventName.get(typeName).callAllHandlers(decryptedMessage);
        } else {
            if (empty(defaultMessageHandlers)) {
                throw new MessagingException(
                    "No handlers for message with event: " + eventName + " and type: " + typeName
                );
            }
            callAllHandlers(defaultMessageHandlers, sns);
        }
    }
}
