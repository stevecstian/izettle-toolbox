package com.izettle.messaging.handler;

import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izettle.messaging.serialization.MessageDeserializer;

public class MessageHandlerForSingleMessageType<M> implements MessageHandler<Message> {
	private static final ObjectMapper jsonMapper = new ObjectMapper();
	private final Class<M> classType;
	private final MessageHandler<M> actualHandler;

	public MessageHandlerForSingleMessageType(Class<M> classType, MessageHandler<M> actualHandler) {
		this.actualHandler = actualHandler;
		this.classType = classType;
	}

	@Override
	public void handle(Message message) throws Exception {
		String messageBody = MessageDeserializer.removeSnsEnvelope(message.getBody());
		M msg = jsonMapper.readValue(messageBody, classType);
		actualHandler.handle(msg);
	}
}
