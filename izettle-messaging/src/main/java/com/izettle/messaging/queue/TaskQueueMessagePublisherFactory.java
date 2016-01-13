package com.izettle.messaging.queue;

import static java.util.Objects.requireNonNull;

import com.izettle.messaging.ExecutorServiceQueuesProcessor;
import com.izettle.messaging.MessagePublisher;
import com.izettle.messaging.serialization.MessageDeserializer;
import com.izettle.messaging.serialization.MessageSerializer;
import java.nio.file.Paths;
import java.util.function.Consumer;

public class TaskQueueMessagePublisherFactory {

    private final String rootFilePath;
    private final H2TaskQueueFactory taskQueueFactory;
    private final ExecutorServiceQueuesProcessor executorServiceQueuesProcessor;

    public TaskQueueMessagePublisherFactory(
        String absoluteRootDirPath,
        H2TaskQueueFactory taskQueueFactory,
        ExecutorServiceQueuesProcessor executorServiceQueuesProcessor
    ) {
        requireNonNull(absoluteRootDirPath);
        requireNonNull(taskQueueFactory);
        requireNonNull(executorServiceQueuesProcessor);

        // Assert that the file path exists, the root path must exist when starting.
        Paths.get(absoluteRootDirPath);

        this.executorServiceQueuesProcessor = executorServiceQueuesProcessor;
        this.taskQueueFactory = taskQueueFactory;
        this.rootFilePath = absoluteRootDirPath;
    }

    public <M> MessagePublisher create(
        String eventName,
        MessagePublisher messagePublisher,
        MessageSerializer messageSerializer,
        MessageDeserializer<M> messageDeserializer,
        Consumer<Exception> exceptionConsumer,
        int numMessagesToPollFromQueue,
        int maxRetriesBeforeDLQ,
        int maxRetriesBeforeRemove,
        int numPollsBetweenDLQPoll
    ) {
        final TaskQueue dlq =
            taskQueueFactory.createWithRetryThresholdEviction(
                Paths.get(rootFilePath, eventName.toLowerCase().replaceAll("\\s+", "_") + "_dlq")
                    .toAbsolutePath()
                    .toString(),
                maxRetriesBeforeRemove
            );

        final TaskQueue taskQueue =
            taskQueueFactory.createWithDeadLetterQueue(
                Paths.get(eventName.toLowerCase().replaceAll("\\s+", "_")).toAbsolutePath().toString(),
                dlq,
                maxRetriesBeforeDLQ
            );

        final TaskSerializationConverter<M> taskSerializationConverter =
            new TaskSerializationConverter<>(messageSerializer, messageDeserializer);

        final TaskQueueMessageRePublisher<M> queuedMessagesPublisher = new TaskQueueMessageRePublisher<>(
            taskQueue,
            numMessagesToPollFromQueue,
            dlq,
            numPollsBetweenDLQPoll,
            messagePublisher,
            eventName,
            taskSerializationConverter,
            exceptionConsumer
        );

        executorServiceQueuesProcessor.add(queuedMessagesPublisher);

        return new TaskQueueBackedMessagePublisher(
            messagePublisher,
            taskQueue,
            taskSerializationConverter,
            exceptionConsumer
        );
    }
}
