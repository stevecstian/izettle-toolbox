package com.izettle.messaging.queue;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.izettle.messaging.MessagePublisher;
import com.izettle.messaging.MessagingException;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Task queue backed message publisher wraps a message publisher with fallback to saving messages on a queue
 * if the messages could not be handled by the exception handler.
 *
 * Note that this class does not try to re-send messages that are put on the message queue to the destination queue.
 * This needs to be handled by task consumer.
 */
public class TaskQueueBackedMessagePublisher implements MessagePublisher {

    private final TaskQueue taskQueue;
    private final TaskSerializationConverter taskSerializationConverter;
    private final MessagePublisher destination;
    private final Consumer<Exception> exceptionHandler;

    public TaskQueueBackedMessagePublisher(
        MessagePublisher destination, TaskQueue taskQueue,
        TaskSerializationConverter taskSerializationConverter, Consumer<Exception> exceptionHandler
    ) {
        requireNonNull(exceptionHandler);
        requireNonNull(taskQueue);
        requireNonNull(taskSerializationConverter);
        requireNonNull(destination);

        this.exceptionHandler = exceptionHandler;
        this.destination = destination;
        this.taskSerializationConverter = taskSerializationConverter;
        this.taskQueue = taskQueue;
    }

    @Override
    public <M> void post(M message, String eventName) throws MessagingException {
        try {
            destination.post(message, eventName);
            return;
        } catch (MessagingException e) {
            exceptionHandler.accept(e);
        }

        try {
            taskQueue.add(taskSerializationConverter.convert(message, eventName));
        } catch (IllegalStateException e) {
            throw new MessagingException("Unable to add message to task queue, queue not accepting task", e);
        } catch (JsonProcessingException e) {
            throw new MessagingException("Unable to add message to task queue, not able to serialize", e);
        }
    }

    @Override
    public <M> void postBatch(Collection<M> messages, String eventName) throws MessagingException {
        try {
            destination.postBatch(messages, eventName);
            return;
        } catch (MessagingException e) {
            exceptionHandler.accept(e);
        }

        try {
            taskQueue.addAll(taskSerializationConverter.convert(messages, eventName));
        } catch (IllegalStateException e) {
            throw new MessagingException("Unable to add message to task queue, queue not accepting task", e);
        } catch (JsonProcessingException e) {
            throw new MessagingException("Unable to add message to task queue, not able to serialize", e);
        }
    }
}

