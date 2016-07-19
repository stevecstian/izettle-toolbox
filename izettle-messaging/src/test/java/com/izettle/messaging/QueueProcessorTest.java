package com.izettle.messaging;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.izettle.messaging.handler.AsyncMessageDispatcher;
import com.izettle.messaging.handler.MessageHandler;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class QueueProcessorTest {
    private MessageQueueProcessor queueProcessor;
    private final AmazonSQS mockAmazonSQS = mock(AmazonSQS.class);
    @SuppressWarnings("unchecked")
    private final MessageHandler<Message> mockHandler = mock(MessageHandler.class);
    private final AsyncMessageDispatcher asyncMockHandler = mock(AsyncMessageDispatcher.class);
    private final List<Message> receivedMessages = new ArrayList<>();

    @Before
    public final void before() throws Exception {
        queueProcessor = QueueProcessor.createQueueProcessor(
                mockAmazonSQS,
                "UnitTestProcessor",
                "testurl",
                "deadLetterQueueUrl",
                mockHandler
        );

        ReceiveMessageResult messageResult = mock(ReceiveMessageResult.class);
        when(mockAmazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(messageResult);
        when(messageResult.getMessages()).thenReturn(receivedMessages);
    }

    private Message createMessage(String messageId) {
        Message msg = new Message();
        msg.setMessageId(messageId);
        msg.setReceiptHandle(messageId);
        return msg;
    }

    @Test
    public void shouldPassAllPolledMessagesToSpecifiedHandler() throws Exception {

        // Arrange
        Message msg1 = createMessage("msg1");
        Message msg2 = createMessage("msg2");
        receivedMessages.add(msg1);
        receivedMessages.add(msg2);

        // Act
        queueProcessor.poll();

        // Assert
        verify(mockHandler).handle(msg1);
        verify(mockHandler).handle(msg2);
        verifyNoMoreInteractions(mockHandler);
    }

    @Test
    public void shouldDeletePolledMessagesAfterHavingPassedThemToTheMessageHandler() throws Exception {
        // Arrange
        Message msg1 = createMessage("testReceiptHandle");
        receivedMessages.add(msg1);

        // Act
        queueProcessor.poll();

        // Assert
        ArgumentCaptor<DeleteMessageRequest> argumentCaptor = ArgumentCaptor.forClass(DeleteMessageRequest.class);
        verify(mockAmazonSQS).deleteMessage(argumentCaptor.capture());
        assertEquals("testReceiptHandle", argumentCaptor.getValue().getReceiptHandle());
    }

    @Test
    public void shouldNotDeletePolledMessagesIfTheHandlerThrowsAnException() throws Exception {
        // Arrange
        Message msg1 = createMessage("msg1");
        receivedMessages.add(msg1);
        doThrow(new Exception()).when(mockHandler).handle(msg1);

        // Act
        queueProcessor.poll();

        // Assert
        verify(mockHandler).handle(msg1);
        verify(mockAmazonSQS, never()).deleteMessage(any(DeleteMessageRequest.class));
    }

    @Test
    public void shouldNotDeletePolledMessagesIfTheHandlerIsAsyncMessageDispatcher() throws Exception {
        // Arrange
        queueProcessor = QueueProcessor.createQueueProcessor(
            mockAmazonSQS,
            "UnitTestProcessor",
            "testurl",
            "deadLetterQueueUrl",
            asyncMockHandler
        );
        Message msg1 = createMessage("msg1");
        receivedMessages.add(msg1);

        // Act
        queueProcessor.poll();

        // Assert
        verify(asyncMockHandler).handle(msg1);
        verify(mockAmazonSQS, never()).deleteMessage(any(DeleteMessageRequest.class));
    }
}
