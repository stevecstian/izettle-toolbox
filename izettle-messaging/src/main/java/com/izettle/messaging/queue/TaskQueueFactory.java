package com.izettle.messaging.queue;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 *
 */
public class TaskQueueFactory {

    public TaskQueueFactory() {
    }

    /**
     * Creates a new TaskQueue in given file location using a pushback strategy that will never remove
     * messages but always push back.
     * @param filePath File path where to put queue file
     * @return
     */
    public TaskQueue createPushbackAll(String filePath) {
        return new H2TaskQueue(
            () -> H2ConnectionProvider.file(filePath),
            new StatementManager(),
            (tasks) -> tasks);
    }

    /**
     * Creates new TasksQueue in given file location using a pushback strategy that will remove messages
     * after specified number of push backs of a given message.
     * @param filePath File path where to put queue file
     * @param maxPushbacks The max number of times an individual message can be pushed back before being removed.
     * @return
     */
    public TaskQueue createWithMaxPushbacks(String filePath, int maxPushbacks) {
        return new H2TaskQueue(
            () -> H2ConnectionProvider.file(filePath),
            new StatementManager(),
            (tasks) -> tasks.stream().filter(t -> t.getPushbackCount() < maxPushbacks).collect(Collectors.toList()));
    }

    public TaskQueue createWithDeadletterQueue(String filePath) {
        final TaskQueue dlq = createPushbackAll(filePath + "_dlq");

        return new H2TaskQueue(
            () -> H2ConnectionProvider.file(filePath),
            new StatementManager(),
            (tasks) -> {
                dlq.add((Collection<Task>)tasks);
                return Collections.EMPTY_LIST;
            });
    }
}
