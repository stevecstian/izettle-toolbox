package com.izettle.messaging.queue;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.izettle.messaging.serialization.MessageSerializer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Serializes messages with a serializer and then creates a new tasks for these.
 */
public class MessagePublisherTaskFactory {

    private final MessageSerializer serializer;

    public MessagePublisherTaskFactory(MessageSerializer serializer) {
        requireNonNull(serializer);

        this.serializer = serializer;
    }

    public <M> Task create(M message, String eventName) throws JsonProcessingException {
        return new Task(serializer.serialize(message), eventName);
    }

    public <M> Collection<Task> create(Collection<M> messages, String eventName) throws JsonProcessingException {
        final List<Task> tasks = new ArrayList<>(messages.size());

        for (M message : messages) {
            tasks.add(create(message, eventName));
        }

        return tasks;
    }
}
