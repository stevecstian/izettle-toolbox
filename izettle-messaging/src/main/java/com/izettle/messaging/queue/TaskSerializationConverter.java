package com.izettle.messaging.queue;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.izettle.messaging.serialization.MessageDeserializer;
import com.izettle.messaging.serialization.MessageSerializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Serializes messages with a serializer and then creates a new tasks for these.
 */
public class TaskSerializationConverter<M> {

    private final MessageSerializer serializer;
    private final MessageDeserializer<M> messageDeserializer;

    public TaskSerializationConverter(MessageSerializer serializer, MessageDeserializer<M> messageDeserializer) {
        this.messageDeserializer = messageDeserializer;
        requireNonNull(serializer);

        this.serializer = serializer;
    }

    public Task convert(M message, String eventName) throws JsonProcessingException {
        return new Task(eventName, serializer.serialize(message));
    }

    public Collection<? extends Task> convert(Collection<M> messages, String eventName) throws JsonProcessingException {
        final List<Task> tasks = new ArrayList<>(messages.size());

        for (M message : messages) {
            tasks.add(convert(message, eventName));
        }

        return tasks;
    }

    public M revert(Task task) throws IOException {
        return messageDeserializer.deserialize(task.getPayload());
    }

    public Collection<M> revert(Collection<? extends Task> tasks) throws IOException {
        final List<M> messages = new ArrayList<>(tasks.size());

        for (Task task : tasks) {
            messages.add(revert(task));
        }

        return messages;
    }
}
