package com.izettle.messaging;

import rx.Observable;

@FunctionalInterface
public interface MessageQueueProducer<M> {

    Observable<MessageReceipt> post(M message) throws MessagingException;

}
