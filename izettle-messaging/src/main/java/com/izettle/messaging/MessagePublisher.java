package com.izettle.messaging;

import java.util.Collection;
import java.util.Map;

public interface MessagePublisher {
    <M> void post(M message, String eventName) throws MessagingException;
    <M> void postBatch(Collection<M> messages, String eventName) throws MessagingException;
    default <M> void post(M message, String eventName, Map<String, String> attributes) throws MessagingException {
        post(message, eventName);
    }
}
