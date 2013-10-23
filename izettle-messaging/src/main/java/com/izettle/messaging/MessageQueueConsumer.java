package com.izettle.messaging;

import java.util.List;

public interface MessageQueueConsumer<M> {
	List<MessageWrapper<M>> poll() throws MessagingException;
	List<MessageWrapper<M>> poll(int messageWaitTimeInSeconds) throws MessagingException;
	void delete(MessageWrapper<M> messageWrapper)throws MessagingException;
}
