package com.izettle.messaging.queue;

import static java.util.Objects.requireNonNull;

/**
 * Queued Tasks represents a task that has been added to a queue and received from it.
 */
public class QueuedTask extends Task {

    private final long id;
    private int retryCount;

    QueuedTask(long id, String type, String payload, int retryCount) {
        super(type, payload);

        requireNonNull(id);
        requireNonNull(retryCount);

        this.retryCount = retryCount;
        this.id = id;
    }

    public long getID() {
        return id;
    }

    public int getRetryCount() {
        return retryCount;
    }
}
