package com.izettle.messaging;

import java.util.List;
import java.util.concurrent.Future;

public interface MessageQueueConsumer<M> {
    Future<List<PolledMessage<M>>> poll() throws MessagingException;
    Future<List<PolledMessage<M>>> poll(int messageWaitTimeInSeconds) throws MessagingException;
    void delete(PolledMessage<M> messageWrapper)throws MessagingException;
}
