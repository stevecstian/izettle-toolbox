package com.izettle.messaging.queue;

import static java.util.Objects.requireNonNull;

import com.izettle.messaging.MessagePublisher;
import com.izettle.messaging.MessageQueueProcessor;
import com.izettle.messaging.MessagingException;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * TaskQueueMessageRePublisher polls an internal TaskQueue for events to send to the original message handler.
 */
public class TaskQueueMessageRePublisher<M> implements MessageQueueProcessor {

    private final TaskQueue taskQueue;
    private final MessagePublisher messagePublisher;
    private final Consumer<Exception> exceptionConsumer;
    private final int queuePollNum;
    private final String eventName;
    private final TaskQueue deadLetterQueue;
    private final int numTimesBetweenDLQPoll;
    private final TaskSerializationConverter<M> taskSerializationConverter;
    private int numTimesPolled;

    public TaskQueueMessageRePublisher(
        TaskQueue taskQueue,
        int numMessagesToPollFromQueue,
        TaskQueue deadLetterQueue,
        int numTimesBetweenDLQPoll,
        MessagePublisher messagePublisher,
        String eventName,
        TaskSerializationConverter<M> taskSerializationConverter,
        Consumer<Exception> exceptionConsumer
    ) {
        requireNonNull(taskQueue);
        requireNonNull(taskSerializationConverter);
        requireNonNull(deadLetterQueue);
        requireNonNull(messagePublisher);
        requireNonNull(exceptionConsumer);
        requireNonNull(eventName);

        this.taskSerializationConverter = taskSerializationConverter;
        this.numTimesBetweenDLQPoll = numTimesBetweenDLQPoll;
        this.deadLetterQueue = deadLetterQueue;
        this.eventName = eventName;
        this.queuePollNum = numMessagesToPollFromQueue;
        this.exceptionConsumer = exceptionConsumer;
        this.messagePublisher = messagePublisher;
        this.taskQueue = taskQueue;
    }

    @Override
    public void poll() throws MessagingException {
        final TaskQueue activeQueue = getActiveQueue();

        final Collection<QueuedTask> tasks;
        try {
            tasks = activeQueue.peek(queuePollNum);
        } catch (IllegalStateException e) {
            // we don't want the thread to die because there is a problem with peeking into the queue.
            exceptionConsumer.accept(e);
            return;
        }

        try {
            final Collection<M> deserializedTasks = taskSerializationConverter.revert(tasks);

            messagePublisher.postBatch(deserializedTasks, eventName);

            activeQueue.remove(tasks);
        } catch (IOException e) {
            // Thrown by deserialization of the Tasks into the correct payload, this shall not yield an exception that
            // is not handled by the tasks.
            activeQueue.retry(tasks);
            exceptionConsumer.accept(e);
        } catch (MessagingException e) {
            activeQueue.retry(tasks);
            // The messaging exception is handled by the toolbox classes and can be thrown and handled by the queue
            // processor thread.
            throw e;
        }
    }

    private TaskQueue getActiveQueue() {
        numTimesPolled++;

        if (numTimesPolled % numTimesBetweenDLQPoll == 0) {
            numTimesPolled = 0;
            return deadLetterQueue;
        }

        return taskQueue;
    }

    @Override
    public String getName() {
        return "TaskQueueConsumer_" + eventName;
    }
}
