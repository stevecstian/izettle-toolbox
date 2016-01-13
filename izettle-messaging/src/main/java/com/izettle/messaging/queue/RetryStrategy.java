package com.izettle.messaging.queue;

import java.util.Collection;

public interface RetryStrategy {

    /**
     * Take decision of which items to push back and what elements to remove. The elements returned will be retried
     * and the elements not returned will be removed.
     *
     * @param tasks List of tasks to decide to pushback or remove
     * @return List of tasks to pushback. The queue items not in this list from the tasks list will be removed.
     */
    Collection<QueuedTask> decide(Collection<QueuedTask> tasks);
}
