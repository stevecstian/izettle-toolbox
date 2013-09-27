package com.izettle.messaging.handler;

/**
 * Implementations of this interface should be able to process messages of the specified type.
 * Implementations MUST be able to handle the fact that a message might be received several times
 * dues to queueing mechanisms and retries. 
 * 
 * @param <M> The type of message that is being handled.
 */
public interface MessageHandler<M> {
	void handle(M message) throws Exception;
}
