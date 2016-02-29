package com.izettle.messaging;

@FunctionalInterface
public interface MessageQueueProducer<M> {

    MessageReceipt post(M message) throws MessagingException;

}
