package com.izettle.messaging.queue;

import java.util.Collection;

public interface TaskQueue {

    void add(Task task);

    void add(Collection<Task> tasks);

    Collection<QueuedTask> peek(int num);

    void remove(Collection<QueuedTask> tasks);

    void pushBack(Collection<QueuedTask> items);
}
