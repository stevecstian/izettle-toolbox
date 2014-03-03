package com.izettle.messaging;

import java.util.Collection;

public interface MessagePublisher {
	<M> void post(M message, String eventName) throws MessagingException;
	<M> void postBatch(Collection<M> messages, String eventName) throws MessagingException;
}
