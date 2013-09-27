package com.izettle.messaging;

import java.util.List;

public interface MessageQueueConsumer<M> {
	List<MessageWrapper<M>> poll() throws MessagingException;
	void delete(MessageWrapper<M> messageWrapper)throws MessagingException;
}
