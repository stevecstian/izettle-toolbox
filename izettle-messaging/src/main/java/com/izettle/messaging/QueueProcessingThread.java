package com.izettle.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a thread that keeps on polling a message queue until interrupted.
 */
public class QueueProcessingThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(QueueProcessingThread.class);
    private final QueueProcessingRunnable runnable;

    public QueueProcessingThread(MessageQueueProcessor queueProcessor) {
        super(queueProcessor.getName());
        this.runnable = new QueueProcessingRunnable(queueProcessor);
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
