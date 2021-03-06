package com.izettle.messaging.handler;

/**
 * Implementations of this interface should be able to process messages of the specified type.
 * Implementations MUST be able to handle the fact that a message might be received several times
 * dues to queueing mechanisms and retries.
 *
 * A message handler can throw a {@code RetryableMessageHandlerException}, to indicate to
 * the callers that the message cannot be handled right now, but should be retried at a later time.
 *
 * @param <M> The type of message that is being handled.
 */
@FunctionalInterface
public interface MessageHandler<M> {
    void handle(M message) throws Exception;
}
