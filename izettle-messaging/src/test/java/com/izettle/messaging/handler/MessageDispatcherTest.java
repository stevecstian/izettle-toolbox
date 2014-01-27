package com.izettle.messaging.handler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.sqs.model.Message;
import com.izettle.messaging.TestMessage;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class MessageDispatcherTest {

	private final MessageDispatcher dispatcher = MessageDispatcher.nonEncryptedMessageDispatcher();
	@SuppressWarnings("unchecked")
	private final MessageHandler<TestMessage> testMessageHandler = mock(MessageHandler.class);
	@SuppressWarnings("unchecked")
	private final MessageHandler<String> stringHandler = mock(MessageHandler.class);

	@Test(expected = com.izettle.messaging.MessagingException.class)
	public void shouldThrowExceptionIfNoMessageHandlersForMessageTypeIsPresent() throws Exception {
		Message message = new Message();
		message.setBody("{\"Subject\":\"com.izettle.messaging.messages.MessageWithoutHandle\", \"Message\": \"{}\"}");

		dispatcher.handle(message);
	}

	@Test(expected = com.izettle.messaging.MessagingException.class)
	public void shouldThrowExceptionIfMessageHasNoSubject() throws Exception {
		Message message = new Message();
		message.setBody("{\"Message\": \"{}\"}");
		dispatcher.handle(message);
	}

	@Test
	public void shouldCallSingleHandlerWhenReceivingMessage() throws Exception {

		dispatcher.addHandler(TestMessage.class, testMessageHandler);

		Message message = new Message();
		message.setBody("{\"Subject\":\"com.izettle.messaging.TestMessage\", \"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");
		dispatcher.handle(message);

		verify(testMessageHandler).handle(any(TestMessage.class));
	}

	@Test
	public void shouldCallCorrectHandlerWhenReceivingMessage() throws Exception {

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

		dispatcher.addHandler(TestMessage.class, testMessageHandler);
		
		Message message = new Message();
		message.setBody("{\"Subject\":\"com.izettle.messaging.TestMessage\", \"Message\": \"{\\\"message\\\":\\\"Hello!\\\"}\"}");
		dispatcher.handle(message);

		ArgumentCaptor<TestMessage> argumentCaptor = ArgumentCaptor.forClass(TestMessage.class);
		verify(testMessageHandler).handle(argumentCaptor.capture());
		TestMessage testMessage = argumentCaptor.getValue();
		assertEquals("Hello!", testMessage.getMessage());
	}

	@Test
	public void shouldCallHandlerForEventNameWhenReceivingMessage() throws Exception {

		// Arrange
		dispatcher.addHandler(String.class, stringHandler);
		dispatcher.addHandler(TestMessage.class, "ForcedEventName", testMessageHandler);

		Message message = new Message();
		message.setBody("{\"Subject\":\"ForcedEventName\", \"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");

		// Act
		dispatcher.handle(message);

		// Assert
		verify(testMessageHandler).handle(any(TestMessage.class));
		verify(stringHandler, never()).handle(any(String.class));
	}
}
