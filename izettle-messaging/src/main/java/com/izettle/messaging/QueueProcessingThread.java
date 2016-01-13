package com.izettle.messaging;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of a thread that keeps on polling a message queue until interrupted.
 */
public class QueueProcessingThread extends Thread {

    private final QueueProcessingRunnable runnable;

    public QueueProcessingThread(MessageQueueProcessor queueProcessor) {
        super(queueProcessor.getName());
        this.runnable = new QueueProcessingRunnable(queueProcessor, 1, TimeUnit.MINUTES);
    }

    public QueueProcessingThread(
        MessageQueueProcessor queueProcessor,
        long sleepDuration,
        TimeUnit sleepDurationTimeUnit
    ) {
        super(queueProcessor.getName());
        this.runnable = new QueueProcessingRunnable(queueProcessor, sleepDuration, sleepDurationTimeUnit);
    }

    @Override
    public void run() {
        runnable.run();
    }

    /**
     * Stops the polling thread and waits for last message completion before returning to caller
     */
    public void shutdown() {
        runnable.shutdown();
        try {
            join();
        } catch (InterruptedException ignored) {
        }
    }
}
