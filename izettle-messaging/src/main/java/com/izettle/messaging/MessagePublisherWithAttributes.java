package com.izettle.messaging;

import java.util.Map;

public interface MessagePublisherWithAttributes extends MessagePublisher {
    <M> void post(M message, String eventName, Map<String, String> attributes) throws MessagingException;
}
