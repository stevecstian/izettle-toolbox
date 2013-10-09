package com.izettle.messaging.handler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.sqs.model.Message;
import com.izettle.messaging.TestMessage;
import com.izettle.messaging.serialization.MessageSerializer;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class MessageDispatcherTest {

	@Test(expected = com.izettle.messaging.MessagingException.class)
	public void shouldThrowExceptionIfNoMessageHandlersForMessageTypeIsPresent() throws Exception {
		MessageDispatcher dispatcher = MessageDispatcher.nonEncryptedMessageDispatcher();
		MessageSerializer<TestMessage> messageSerializer = new MessageSerializer<>();
		Message message = new Message();
		message.setBody(messageSerializer.serialize(new TestMessage("")));
		message.setBody("{\"Subject\":\"com.izettle.messaging.messages.MessageWithoutHandle\", \"Message\": \"{}\"}");

		dispatcher.handle(message);
	}

	@Test
	public void shouldCallSingleHandlerWhenReceivingMessage() throws Exception {
		MessageDispatcher dispatcher = MessageDispatcher.nonEncryptedMessageDispatcher();

		@SuppressWarnings("unchecked")
		MessageHandler<TestMessage> testMessageHandler = mock(MessageHandler.class);

		dispatcher.addHandler(TestMessage.class, testMessageHandler);

		Message message = new Message();
		message.setBody("{\"Subject\":\"com.izettle.messaging.TestMessage\", \"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");
		dispatcher.handle(message);

		verify(testMessageHandler).handle(any(TestMessage.class));
	}

	@Test
	public void shouldCallCorrectHandlerWhenReceivingMessage() throws Exception {
		MessageDispatcher dispatcher = MessageDispatcher.nonEncryptedMessageDispatcher();

		@SuppressWarnings("unchecked")
		MessageHandler<TestMessage> testMessageHandler = mock(MessageHandler.class);

		@SuppressWarnings("unchecked")
		MessageHandler<String> stringHandler = mock(MessageHandler.class);

		dispatcher.addHandler(TestMessage.class, testMessageHandler);
		dispatcher.addHandler(String.class, stringHandler);
		
		Message message = new Message();
		message.setBody("{\"Subject\":\"com.izettle.messaging.TestMessage\", \"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");
		dispatcher.handle(message);
		
		verify(testMessageHandler).handle(any(TestMessage.class));
		verify(stringHandler, never()).handle(any(String.class));
	}

	@Test
	public void shouldDeserializeMessageFromJson() throws Exception {
		MessageDispatcher dispatcher = MessageDispatcher.nonEncryptedMessageDispatcher();

		@SuppressWarnings("unchecked")
		MessageHandler<TestMessage> testMessageHandler = mock(MessageHandler.class);

		dispatcher.addHandler(TestMessage.class, testMessageHandler);
		
		Message message = new Message();
		message.setBody("{\"Subject\":\"com.izettle.messaging.TestMessage\", \"Message\": \"{\\\"message\\\":\\\"Hello!\\\"}\"}");
		dispatcher.handle(message);

		ArgumentCaptor<TestMessage> argumentCaptor = ArgumentCaptor.forClass(TestMessage.class);
		verify(testMessageHandler).handle(argumentCaptor.capture());
		TestMessage testMessage = argumentCaptor.getValue();
		assertEquals("Hello!", testMessage.getMessage());
	}
}
