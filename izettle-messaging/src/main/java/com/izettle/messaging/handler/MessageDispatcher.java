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
    private static final ObjectMapper JSON_MAPPER = JsonSerializer.getInstance();

    public static MessageDispatcher nonEncryptedMessageDispatcher() {
        return new MessageDispatcher();
    }

    public static MessageDispatcher encryptedMessageDispatcher(byte[] privatePgpKey, final String privatePgpKeyPassphrase) throws MessagingException {
        if (empty(privatePgpKey) || empty(privatePgpKeyPassphrase)) {
            throw new MessagingException("Can't create encryptedMessageDispatcher with private PGP key as null or privatePgpKeyPassphrase as null");
        }
        return new MessageDispatcher(privatePgpKey, privatePgpKeyPassphrase);
    }

    private MessageDispatcher() {
        this.messageDeserializer = new MessageDeserializer<>(String.class);
    }

    private MessageDispatcher(byte[] privatePgpKey, String privatePgpKeyPassphrase) {
        this.messageDeserializer = new MessageDeserializer<>(String.class, privatePgpKey, privatePgpKeyPassphrase);
    }

    private static class ListOfMessageHandlersForType<M> {
        private final Class<M> messageType;
        public final List<MessageHandler<M>> handlers = new ArrayList<>();
        private static final ObjectMapper JSON_MAPPER = JsonSerializer.getInstance();

        public ListOfMessageHandlersForType(Class<M> messageType) {
            this.messageType = messageType;
        }
        public void add(MessageHandler<M> handler) {
            handlers.add(handler);
        }
        public void callAllHandlers(String message) throws Exception {
            M msg = JSON_MAPPER.readValue(message, messageType);
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
        AmazonSNSMessage sns = JSON_MAPPER.readValue(messageBody, AmazonSNSMessage.class);
        String decryptedMessage = messageDeserializer.decrypt(sns.getMessage());
        String eventName = sns.getSubject();
        if (empty(eventName)) {
            throw new MessagingException(
                    "Received message without event name. "
                    + "MessageDispatcher requires a message subject in order to know which handler to route the message to. "
                    + "Make sure that you publish your message with a message subject before trying to receive it."
            );
        }

        if (messageHandlersPerEventName.containsKey(eventName)) {
            messageHandlersPerEventName.get(eventName).callAllHandlers(decryptedMessage);
        } else {
            if (empty(defaultMessageHandlers)) {
                throw new MessagingException("No handlers for event " + eventName);
            }
            callAllHandlers(defaultMessageHandlers, sns);
        }
    }
}
