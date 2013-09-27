package com.izettle.messaging;

import com.izettle.java.UUIDFactory;

/**
 * Wrapper for message received from message queue. Holds both the message itself, and a handle to the message
 * to be able to delete it from queue.
 *
 * @param <M> Message type.
 */
public class MessageWrapper<M> {
	private final M message;
	private final String messageReceiptHandle;
	private final String md5Hash;
	private final String messageId;

	/**
	 * Creates wrapper for Amazon SQS message.
	 *
	 * @param message Message.
	 * @param messageReceiptHandle SQS message receipt handle.
	 * @param md5Hash A hash of the message content.
	 * @param messageId The SQS message id.
	 */
	public MessageWrapper(M message, String messageReceiptHandle, String md5Hash, String messageId) {
		this.message = message;
		this.messageReceiptHandle = messageReceiptHandle;
		this.md5Hash = md5Hash;
		this.messageId = messageId;
	}

	/**
	 * Creates wrapper for JMS message.
	 *
	 * @param message Message.
	 * @param md5Hash A hash of the message content.
	 */
	public MessageWrapper(M message, String md5Hash) {
		this.message = message;
		this.messageReceiptHandle = null;
		this.md5Hash = md5Hash;
		this.messageId = UUIDFactory.createAsString();
	}

	public M getMessage() {
		return message;
	}

	public String getMd5Hash() {
		return md5Hash;
	}

	public String getMessageReceiptHandle() {
		return messageReceiptHandle;
	}

	public String getMessageId() {
		return messageId;
	}
}
