package com.izettle.messaging;

import java.util.concurrent.Future;

@FunctionalInterface
public interface MessageQueueProducer<M> {

    Future<MessageReceipt> post(M message) throws MessagingException;

}
