package com.izettle.messaging.queue;

import static java.util.Objects.requireNonNull;

import com.izettle.messaging.MessagePublisher;
import com.izettle.messaging.MessageQueueProcessor;
import com.izettle.messaging.MessagingException;
import java.util.Collection;

public class QueuedMessagePublisher implements MessagePublisher, MessageQueueProcessor {

    private final TaskQueueBackedMessagePublisher messagePublisher;
    private final TaskQueueMessageRePublisher taskQueueMessageRePublisher;

    public QueuedMessagePublisher(
        TaskQueueBackedMessagePublisher messagePublisher,
        TaskQueueMessageRePublisher taskQueueMessageRePublisher
    ) {
        requireNonNull(messagePublisher);
        requireNonNull(taskQueueMessageRePublisher);

        this.messagePublisher = messagePublisher;
        this.taskQueueMessageRePublisher = taskQueueMessageRePublisher;
    }

    @Override
    public <M> void post(M message, String eventName) throws MessagingException {
        messagePublisher.post(message, eventName);
    }

    @Override
    public <M> void postBatch(Collection<M> messages, String eventName) throws MessagingException {
        messagePublisher.postBatch(messages, eventName);
    }

    @Override
    public void poll() throws MessagingException {
        taskQueueMessageRePublisher.poll();
    }

    @Override
    public String getName() {
        return taskQueueMessageRePublisher.getName();
    }
}
