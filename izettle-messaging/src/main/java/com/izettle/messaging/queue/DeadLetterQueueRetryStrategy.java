package com.izettle.messaging.queue;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Moves retried messages to a different queue if they have been retried more than specified threshold
 */
public class DeadLetterQueueRetryStrategy implements RetryStrategy {

    private final TaskQueue dlq;
    private final int maxRetries;

    /**
     * @param dlq The dead letter queue to handle messages that have had too many retries
     * @param maxRetries The max number of retries of a message before being put on the dead letter queue.
     */
    public DeadLetterQueueRetryStrategy(TaskQueue dlq, int maxRetries) {
        requireNonNull(dlq);

        this.maxRetries = maxRetries;
        this.dlq = dlq;
    }

    @Override
    public Collection<QueuedTask> decide(Collection<QueuedTask> tasks) {
        final List<QueuedTask> tasksToEnqueueToDLQ = new LinkedList<>();
        final List<QueuedTask> tasksToRetry = new LinkedList<>();

        tasks.forEach(t -> (t.getRetryCount() > maxRetries ? tasksToEnqueueToDLQ : tasksToRetry).add(t));

        dlq.addAll(tasksToEnqueueToDLQ);

        return tasksToRetry;
    }
}
