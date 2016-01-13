package com.izettle.messaging.queue;

import java.sql.Connection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class H2TaskQueueFactory {

    private Function<String, Supplier<Connection>> connectionSupplierFunction;

    public H2TaskQueueFactory(Function<String, Supplier<Connection>> connectionSupplierFunction) {
        this.connectionSupplierFunction = connectionSupplierFunction;
    }

    /**
     * Creates a new TaskQueue in given file location. Uses a always retry strategy
     * @param filePath File path where to put queue file
     * @return
     */
    public TaskQueue create(String filePath) {
        return new H2TaskQueue(
            connectionSupplierFunction.apply(filePath),
            new H2StatementManager(),
            (tasks) -> tasks);
    }

    /**
     * Creates a new TaskQueue in given file location. Uses a always retry strategy
     * @param filePath File path where to put queue file
     * @return
     */
    public TaskQueue createWithRetryThresholdEviction(String filePath, long maxRetries) {
        return new H2TaskQueue(
            connectionSupplierFunction.apply(filePath),
            new H2StatementManager(),
            (tasks) -> tasks.stream().filter(t -> t.getRetryCount() < maxRetries).collect(Collectors.toList())
        );
    }

    /**
     * Creates a new TaskQueue in given file location. Will move tasks to dead letter queue when they have been
     * retried more than specified amount of times.
     * @param filePath The file path to create the original task queue
     * @param dlq The dlq to move tasks into if they have been retried more than a specific number of times.
     * @param maxRetries Max retries a task can have before being moved into DLQ.
     * @return task queue
     */
    public TaskQueue createWithDeadLetterQueue(String filePath, TaskQueue dlq, int maxRetries) {
        return new H2TaskQueue(
            connectionSupplierFunction.apply(filePath),
            new H2StatementManager(),
            new DeadLetterQueueRetryStrategy(dlq, maxRetries));
    }
}
