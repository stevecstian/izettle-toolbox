package com.izettle.messaging;

import static java.util.Objects.requireNonNull;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ExecutorServiceQueuesProcessor implements MessageQueueProcessor {

    private final ExecutorService executorService;
    private final List<MessageQueueProcessor> queueProcessors;
    private final Consumer<Exception> exceptionConsumer;
    private final int timeToSleepBetweenDoneChecks;
    private final TimeUnit timeToSleepBetweenDoneChecksTimeUnit;

    public ExecutorServiceQueuesProcessor(
        ExecutorService executorService,
        Consumer<Exception> exceptionConsumer,
        int timeToSleepBetweenDoneChecks,
        TimeUnit timeToSleepBetweenDoneChecksTimeUnit
    ) {
        requireNonNull(executorService);
        requireNonNull(exceptionConsumer);
        requireNonNull(timeToSleepBetweenDoneChecks);

        this.timeToSleepBetweenDoneChecks = timeToSleepBetweenDoneChecks;
        this.timeToSleepBetweenDoneChecksTimeUnit = timeToSleepBetweenDoneChecksTimeUnit;

        this.exceptionConsumer = exceptionConsumer;
        this.executorService = executorService;

        this.queueProcessors = new LinkedList<>();
        this.queueProcessors.addAll(queueProcessors);
    }

    public void add(MessageQueueProcessor messageQueueProcessor) {
        queueProcessors.add(messageQueueProcessor);
    }

    @Override
    public void poll() throws MessagingException {
        try {
            final List<Callable<Void>> pollTasks = queueProcessors.stream().map(
                qp -> (Callable<Void>) () -> {
                    try {
                        qp.poll();
                    } catch (Exception e) {
                        // Yes we do a bad catch all exception here but we do not want the exception of one
                        // of the queue processors to affect the rest of the queue processors. If one job throws
                        // then the ExecutorService will stop.
                        exceptionConsumer.accept(
                            new MessagingException(
                                "QueueProcessor " + qp.getName() + " could not poll",
                                e
                            )
                        );
                    }

                    return null;
                }

            ).collect(Collectors.toList());

            final List<Future<Void>> resultFutures = executorService.invokeAll(pollTasks);

            while (resultFutures.stream().anyMatch(f -> !f.isDone())) {
                Thread.sleep(timeToSleepBetweenDoneChecksTimeUnit.toMillis(timeToSleepBetweenDoneChecks));
            }

        } catch (InterruptedException e) {
            exceptionConsumer.accept(e);
        }
    }

    @Override
    public String getName() {
        return "ExecutorServiceQueueProcessor";
    }

    public void shutdown(long timeout, TimeUnit timeUnit) throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(timeout, timeUnit);
    }
}
