package com.izettle.messaging;

import java.util.List;

public interface MessageQueueConsumer<M> {
	List<PolledMessage<M>> poll() throws MessagingException;
	List<PolledMessage<M>> poll(int messageWaitTimeInSeconds) throws MessagingException;
	void delete(PolledMessage<M> messageWrapper)throws MessagingException;
}
