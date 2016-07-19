package com.izettle.messaging.handler;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.sqs.model.Message;
import com.izettle.messaging.MessagingException;
import com.izettle.messaging.TestMessage;
import com.izettle.messaging.serialization.AmazonSNSMessage;
import com.izettle.messaging.serialization.AmazonSQSMessage;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsyncMessageDispatcherTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    private final AsyncMessageDispatcher dispatcher = AsyncMessageDispatcher.nonEncryptedMessageDispatcher();
    @Mock
    private MessageHandler<AmazonSQSMessage> testMessageHandler;
    @Mock
    private MessageHandler<AmazonSQSMessage> stringHandler;
    @Mock
    private MessageHandler<AmazonSQSMessage> testSQSMessageHandler;

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
        thrown.expectMessage("Received message without event name or type. AsyncMessageDispatcher requires a message "
            + "subject or type in order to know which handler to route the message to. Make sure that you publish your "
            + "message with a message subject or type before trying to receive it.\n"
            + "null");
        dispatcher.handle(message);
    }

    @Test
    public void shouldCallSingleHandlerWhenReceivingMessage() throws Exception {

        dispatcher.addHandler(AmazonSQSMessage.class, "Test", testMessageHandler);

        Message message = new Message();
        message.setBody(
            "{\"Subject\":\"Test\", \"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");
        dispatcher.handle(message);

        verify(testMessageHandler).handle(any(AmazonSQSMessage.class));
    }

    @Test
    public void shouldCallTypeHandlerWhenNoSubjectSet() throws Exception {

        dispatcher.addHandler(AmazonSQSMessage.class, "Test", testMessageHandler);

        Message message = new Message();
        message.setBody(
            "{\"Type\":\"Test\", \"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");
        dispatcher.handle(message);

        verify(testMessageHandler).handle(any(AmazonSQSMessage.class));
    }

    @Test
    public void shouldCallCorrectHandlerWhenReceivingMessage() throws Exception {

        dispatcher.addHandler(AmazonSQSMessage.class, "Test", testMessageHandler);
        dispatcher.addHandler(AmazonSQSMessage.class, "String", stringHandler);

        Message message = new Message();
        message.setBody(
            "{\"Subject\":\"Test\", \"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");
        dispatcher.handle(message);

        verify(testMessageHandler).handle(any(AmazonSQSMessage.class));
        verify(stringHandler, never()).handle(any(AmazonSQSMessage.class));
    }

    @Test
    public void shouldPrioritizeSubjectOverType() throws Exception {

        dispatcher.addHandler(AmazonSQSMessage.class, "Test", testMessageHandler);
        dispatcher.addHandler(AmazonSQSMessage.class, "String", stringHandler);

        Message message = new Message();
        message.setBody(
            "{\"Subject\":\"Test\", \"Type\":\"String\", "
                + "\"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");
        dispatcher.handle(message);

        verify(testMessageHandler).handle(any(AmazonSQSMessage.class));
        verify(stringHandler, never()).handle(any(AmazonSQSMessage.class));
    }

    @Test
    public void shouldContainSNSMessageWithMessageField() throws Exception {

        dispatcher.addHandler(AmazonSQSMessage.class, "Test", testMessageHandler);

        Message message = new Message();
        message.setBody(
            "{\"Subject\":\"Test\", \"Message\": \"Hello!\"}");
        dispatcher.handle(message);

        ArgumentCaptor<AmazonSQSMessage> argumentCaptor = ArgumentCaptor.forClass(AmazonSQSMessage.class);
        verify(testMessageHandler).handle(argumentCaptor.capture());
        AmazonSNSMessage testMessage = argumentCaptor.getValue().getBody();
        assertEquals("Hello!", testMessage.getMessage());
    }

    @Test
    public void shouldCallDefaultHandlerWhenReceivingMessageAndNoOtherMessageHandlersMatch() throws Exception {

        dispatcher.addHandler(AmazonSQSMessage.class, "Test", testMessageHandler);
        dispatcher.addDefaultHandler(testSQSMessageHandler);

        Message message = new Message();
        message.setBody("{\"Subject\":\"MessageTypeThatDoesNotMatch\", \"Message\": \"{\\\"message\\\":\\\"\\\"}\"}");
        dispatcher.handle(message);

        verify(testSQSMessageHandler).handle(any(AmazonSQSMessage.class));
        verify(testMessageHandler, never()).handle(any(AmazonSQSMessage.class));
    }
}
