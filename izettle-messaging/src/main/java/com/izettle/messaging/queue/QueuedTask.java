package com.izettle.messaging.queue;

import static java.util.Objects.requireNonNull;

/**
 * Queued Tasks represents a task that has been added to a queue and received from it.
 */
public class QueuedTask extends Task {

    private final long id;
    private int pushbackCount;

    QueuedTask(long id, String type, String payload, int pushbackCount) {
        super(type, payload);

        requireNonNull(id);
        requireNonNull(pushbackCount);

        this.pushbackCount = pushbackCount;
        this.id = id;
    }

    public long getID() {
        return id;
    }

    public int getPushbackCount() {
        return pushbackCount;
    }
}
