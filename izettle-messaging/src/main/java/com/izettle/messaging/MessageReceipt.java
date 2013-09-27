package com.izettle.messaging;

public class MessageReceipt {

	private final String messageId;
	private final String messageString;

	public MessageReceipt(String messageId, String messageString) {
		this.messageId = messageId;
		this.messageString = messageString;
	}

	public String getMessageId() {
		return messageId;
	}

	public String getMessageString() {
		return messageString;
	}

	@Override
	public String toString() {
		return "MessageReceipt{"
				+ "messageId='" + messageId + '\''
				+ ", messageString='" + messageString + '\''
				+ '}';
	}
}
