package com.izettle.alb;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Interface for classes that implement a condition check.
 */
public interface Condition {

    /**
     * Await a precondition is met for a specified max duration.
     * @param executorServiceSupplier Supplier of a executor service
     * @param notificationsConsumer A consumer that can take string messages from the precondition
     * @return
     */
    CompletableFuture<Boolean> check(
        Supplier<ExecutorService> executorServiceSupplier,
        Consumer<String> notificationsConsumer
    );
}
