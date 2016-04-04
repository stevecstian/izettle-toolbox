package com.izettle.messaging.handler;

import com.amazonaws.services.sqs.model.Message;
import com.izettle.messaging.serialization.MessageDeserializer;

public class MessageHandlerForSingleMessageType<M> implements MessageHandler<Message> {
    private final MessageHandler<M> actualHandler;
    private final MessageDeserializer<M> messageDeserializer;

    public MessageHandlerForSingleMessageType(MessageHandler<M> actualHandler, Class<M> classType) {
        this(actualHandler, new MessageDeserializer<>(classType));
    }

    public MessageHandlerForSingleMessageType(MessageHandler<M> actualHandler, MessageDeserializer<M> messageDeserializer) {
        this.actualHandler = actualHandler;
        this.messageDeserializer = messageDeserializer;
    }

    @Override
    public void handle(Message message) throws Exception {
        String messageBody = MessageDeserializer.removeSnsEnvelope(message.getBody());
        String decryptedMessage = messageDeserializer.decrypt(messageBody);
        M msg = messageDeserializer.deserialize(decryptedMessage);
        actualHandler.handle(msg);
    }
}
