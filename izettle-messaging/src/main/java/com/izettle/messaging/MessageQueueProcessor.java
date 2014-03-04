package com.izettle.messaging;

public interface MessageQueueProcessor {
	void poll() throws MessagingException;
	String getName();
}
