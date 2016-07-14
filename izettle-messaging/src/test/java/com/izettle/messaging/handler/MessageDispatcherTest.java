package com.izettle.messaging.handler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.sqs.model.Message;
import com.izettle.messaging.MessagingException;
import com.izettle.messaging.TestMessage;
import com.izettle.messaging.serialization.AmazonSNSMessage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MessageDispatcherTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private final MessageDispatcher dispatcher = MessageDispatcher.nonEncryptedMessageDispatcher();
    @Mock
    private MessageHandler<TestMessage> testMessageHandler;
    @Mock
    private MessageHandler<String> stringHandler;
    @Mock
    private MessageHandler<AmazonSNSMessage> testSNSMessageHandler;

    @Test
    public void shouldThrowExceptionIfNoMessageHandlersForMessageTypeIsPresent() throws Exception {
        Message message = new Message();
        message.setBody("{\"Subject\":\"com.izettle.messaging.messages.MessageWithoutHandle\", "
            + "\"Type\":\"com.izettle.messaging.messages.TestMessage\", "
            + "\"Message\": \"{}\"}");
        thrown.expect(MessagingException.class);
        thrown.expectMessage("No handlers for message with event: com.izettle.messaging.messages.MessageWithoutHandle "
            + "and type: com.izettle.messaging.messages.TestMessage");
        dispatcher.handle(message);
    }

    @Test
    public void shouldThrowExceptionIfMessageHasNoSubjectOrType() throws Exception {
        Message message = new Message();
        message.setBody("{\"Message\": \"{}\"}");
        thrown.expect(MessagingException.class);
        thrown.expectMessage("Received message without event name or type. MessageDispatcher requires a message "
            + "subject or type in order to know which handler to route the message to. Make sure that you publish your "
            + "message with a message subject or type before trying to receive it.\n"
            + "null");
        dispatcher.handle(message);
    }

    @Test
    public void shouldCallSingleHandlerWhenReceivingMessage() throws Exception {

        dispatcher.addHandler(TestMessage.class, testMessageHandler);

        Message message = new Message();
        message.setBody(
            "{\"Subject\":\"com.izettle.messaging.TestMessage\", \"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");
        dispatcher.handle(message);

        verify(testMessageHandler).handle(any(TestMessage.class));
    }

    @Test
    public void shouldCallTypeHandlerWhenNoSubjectSet() throws Exception {

        dispatcher.addHandler(TestMessage.class, testMessageHandler);

        Message message = new Message();
        message.setBody(
            "{\"Type\":\"com.izettle.messaging.TestMessage\", \"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");
        dispatcher.handle(message);

        verify(testMessageHandler).handle(any(TestMessage.class));
    }

    @Test
    public void shouldCallCorrectHandlerWhenReceivingMessage() throws Exception {

        dispatcher.addHandler(TestMessage.class, testMessageHandler);
        dispatcher.addHandler(String.class, stringHandler);

        Message message = new Message();
        message.setBody(
            "{\"Subject\":\"com.izettle.messaging.TestMessage\", \"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");
        dispatcher.handle(message);

        verify(testMessageHandler).handle(any(TestMessage.class));
        verify(stringHandler, never()).handle(any(String.class));
    }

    @Test
    public void shouldPrioritizeEventOverType() throws Exception {

        dispatcher.addHandler(TestMessage.class, testMessageHandler);
        dispatcher.addHandler(String.class, stringHandler);

        Message message = new Message();
        message.setBody(
            "{\"Subject\":\"com.izettle.messaging.TestMessage\", \"Type\":\"java.util.String\", "
            + "\"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");
        dispatcher.handle(message);

        verify(testMessageHandler).handle(any(TestMessage.class));
        verify(stringHandler, never()).handle(any(String.class));
    }

    @Test
    public void shouldDeserializeMessageFromJson() throws Exception {

        dispatcher.addHandler(TestMessage.class, testMessageHandler);

        Message message = new Message();
        message.setBody(
            "{\"Subject\":\"com.izettle.messaging.TestMessage\", \"Message\": \"{\\\"message\\\":\\\"Hello!\\\"}\"}");
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

    @Test
    public void shouldCallDefaultHandlerWhenReceivingMessageAndNoOtherMessageHandlersMatch() throws Exception {

        dispatcher.addHandler(TestMessage.class, testMessageHandler);
        dispatcher.addDefaultHandler(testSNSMessageHandler);

        Message message = new Message();
        message.setBody("{\"Subject\":\"MessageTypeThatDoesNotMatch\", \"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");
        dispatcher.handle(message);

        verify(testSNSMessageHandler).handle(any(AmazonSNSMessage.class));
        verify(testMessageHandler, never()).handle(any(TestMessage.class));
    }
}
