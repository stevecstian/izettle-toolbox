package com.izettle.messaging;

import java.util.Collection;
import java.util.concurrent.Future;

public interface MessagePublisher {
    <M> void post(M message, String eventName) throws MessagingException;
    <M> void postBatch(Collection<M> messages, String eventName) throws MessagingException;
    default <M> Future<?> postAsync(M message, String eventName) throws MessagingException {
        throw new UnsupportedOperationException("postAsync is not implemented yet for eventName : " + eventName);
    }
}
