package com.izettle.messaging;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of a thread that keeps on polling a message queue until interrupted.
 */
public class QueueProcessingThread extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(QueueProcessingThread.class);
    private final String name;
    private final MessageQueueProcessor queueProcessor;

    public QueueProcessingThread(MessageQueueProcessor queueProcessor) {
        super(queueProcessor.getName());
        this.name = queueProcessor.getName();
        this.queueProcessor = queueProcessor;
    }

    @Override
    public void run() {

        LOG.info(String.format("Message queue processor %s started.", name));

        while (!isInterrupted()) {
            try {
                queueProcessor.poll();
            } catch (MessagingException e) {
                LOG.error(String.format("Message queue processor %s failed to poll for new messages.", name), e);
                try {
                    sleep(TimeUnit.MINUTES.toMillis(1));
                } catch (InterruptedException e1) {
                    break;
                }
            }
        }

        LOG.info(String.format("Message queue processor %s stopped.", name));
    }

    /**
     * Stops the polling thread and waits for last message completion before returning to caller
     */
    public void shutdown() {
        interrupt();
        try {
            join();
        } catch (InterruptedException ignored) {
        }
    }
}
