package com.izettle.messaging.handler;

import static com.izettle.java.ValueChecks.allEmpty;
import static com.izettle.java.ValueChecks.anyEmpty;
import static com.izettle.java.ValueChecks.empty;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.izettle.messaging.MessagingException;
import com.izettle.messaging.serialization.AmazonSNSMessage;
import com.izettle.messaging.serialization.AmazonSQSMessage;
import com.izettle.messaging.serialization.JsonSerializer;
import com.izettle.messaging.serialization.MessageDeserializer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Routes messages to other MessageHandlers based on message type. All handlers added with the
 * addHandler() method will be called whenever a message of the supplied type is received by this class.
 * The handlers need to delete the messages from the queue after handling them, using the receipt handle in
 * the AmazonSQSMessage.
 *
 * The received message will need either a Subject or a Type field in the body, as the value of one of these fields
 * is used to map to a handler.
 */
public class AsyncMessageDispatcher implements MessageHandler<Message> {
    private final MessageDeserializer<String> messageDeserializer;
    private final Map<String, ListOfMessageHandlersForType> messageHandlersPerEventName = new ConcurrentHashMap<>();
    private final List<MessageHandler<AmazonSQSMessage>> defaultMessageHandlers = new ArrayList<>();
    private static final ObjectMapper JSON_MAPPER = JsonSerializer.getInstance();

    public static AsyncMessageDispatcher nonEncryptedMessageDispatcher() {
        return new AsyncMessageDispatcher();
    }

    public static AsyncMessageDispatcher encryptedMessageDispatcher(
        byte[] privatePgpKey,
        final String privatePgpKeyPassphrase
    ) throws MessagingException {
        if (anyEmpty(privatePgpKey, privatePgpKeyPassphrase)) {
            throw new MessagingException(
                "Can't create encryptedMessageDispatcher with private PGP key as null or "
                + "privatePgpKeyPassphrase as null"
            );
        }
        return new AsyncMessageDispatcher(privatePgpKey, privatePgpKeyPassphrase);
    }

    private AsyncMessageDispatcher() {
        this.messageDeserializer = new MessageDeserializer<>(String.class);
    }

    private AsyncMessageDispatcher(byte[] privatePgpKey, String privatePgpKeyPassphrase) {
        this.messageDeserializer = new MessageDeserializer<>(String.class, privatePgpKey, privatePgpKeyPassphrase);
    }

    private static class ListOfMessageHandlersForType<M> {
        private final Class<M> messageType;
        public final List<MessageHandler<M>> handlers = new ArrayList<>();
        private static final ObjectMapper JSON_MAPPER = JsonSerializer.getInstance();

        ListOfMessageHandlersForType(Class<M> messageType) {
            this.messageType = messageType;
        }
        public void add(MessageHandler<M> handler) {
            handlers.add(handler);
        }
        public void callAllHandlers(String message) throws Exception {
            M msg = JSON_MAPPER.readValue(message, messageType);
            AsyncMessageDispatcher.callAllHandlers(handlers, msg);
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
        ListOfMessageHandlersForType<M> listOfMessageHandlersForType =
            (ListOfMessageHandlersForType<M>) messageHandlersPerEventName.get(eventName);

        listOfMessageHandlersForType.add(handler);
    }

    public <M> void addHandler(Class<M> classType, MessageHandler<M> handler) {
        addHandler(classType, classType.getName(), handler);
    }

    /*
     * Adds a message handler that should be called if none of the other message handlers match the incoming message.
     */
    public void addDefaultHandler(MessageHandler<AmazonSQSMessage> handler) {
        defaultMessageHandlers.add(handler);
    }

    public void handle(Message message) throws Exception {
        String messageBody = message.getBody();
        AmazonSNSMessage sns = JSON_MAPPER.readValue(messageBody, AmazonSNSMessage.class);
        AmazonSQSMessage sqs = new AmazonSQSMessage(sns, message.getReceiptHandle());
        String eventName = sns.getSubject();
        String typeName = sns.getType();
        String decryptedMessage = messageDeserializer.decrypt(JSON_MAPPER.writeValueAsString(sqs));
        if (allEmpty(eventName, typeName)) {
            throw new MessagingException(
                "Received message without event name or type. AsyncMessageDispatcher requires a message subject or "
                + "type in order to know which handler to route the message to. Make sure that you publish your "
                + "message with a message subject or type before trying to receive it."
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
            callAllHandlers(defaultMessageHandlers, sqs);
        }
    }
}

