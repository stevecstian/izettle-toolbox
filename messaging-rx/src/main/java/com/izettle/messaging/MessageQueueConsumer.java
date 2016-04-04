package com.izettle.messaging;

import java.util.List;
import rx.Observable;

public interface MessageQueueConsumer<M> {
    Observable<List<PolledMessage<M>>> poll() throws MessagingException;
    Observable<List<PolledMessage<M>>> poll(int messageWaitTimeInSeconds) throws MessagingException;
    void delete(PolledMessage<M> messageWrapper)throws MessagingException;
}
