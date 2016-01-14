package com.izettle.messaging.queue;

import java.util.Collection;

public interface TaskQueue {

    int size();

    void add(Task task);

    void addAll(Collection<? extends Task> tasks);

    Collection<QueuedTask> peek(int num);

    void remove(Collection<QueuedTask> tasks);

    void retry(Collection<QueuedTask> tasks);
}
