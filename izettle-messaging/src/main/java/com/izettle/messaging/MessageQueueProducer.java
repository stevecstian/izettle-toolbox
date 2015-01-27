package com.izettle.messaging;

public interface MessageQueueProducer<M> {

    MessageReceipt post(M message) throws MessagingException;

}
