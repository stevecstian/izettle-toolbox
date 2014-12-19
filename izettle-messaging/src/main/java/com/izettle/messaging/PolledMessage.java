package com.izettle.messaging;

import com.izettle.java.uuid.UUIDFactory;

/**
 * Wrapper for message received from message queue. Holds both the message itself, and a handle to the message
 * to be able to delete it from queue.
 *
 * @param <M> Message type.
 */
public class PolledMessage<M> {
	private final M message;
	private final String messageId;

	/**
	 * Creates wrapper for Amazon SQS message.
	 *
	 * @param message Message.
	 * @param messageId SQS message receipt handle.
	 */
	public PolledMessage(M message, String messageId) {
		this.message = message;
		this.messageId = messageId;
	}

	/**
	 * Creates wrapper for JMS message.
	 *
	 * @param message Message.
	 */
	public PolledMessage(M message) {
		this.message = message;
		this.messageId = UUIDFactory.createUUID4AsString();
	}

	public M getMessage() {
		return message;
	}

	public String getMessageId() {
		return messageId;
	}
}
