package com.izettle.messaging.queue;

import static java.util.Objects.requireNonNull;

/**
 * Task represents a client task that is to be added to a queue. This entity does not yet
 * have a ID or other queue related fields, only fields a client can produce
 */
public class Task {

    private final String type;
    private final String payload;

    public Task(String type, String payload) {
        requireNonNull(type);
        requireNonNull(payload);

        this.type = type;
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public String getPayload() {
        return payload;
    }
}
