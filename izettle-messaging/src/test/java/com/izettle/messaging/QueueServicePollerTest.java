package com.izettle.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class QueueServicePollerTest {

    private MessageQueueConsumer<TestMessage> queueServicePoller;
    private AmazonSQS mockAmazonSQS = mock(AmazonSQS.class);

    @Before
    public final void before() throws Exception {
        queueServicePoller = QueueServicePoller.nonEncryptedMessageQueueConsumer(TestMessage.class, "queueUrl", mockAmazonSQS);
    }

    @Test
    public void pollAndDeleteMessageShouldWork() throws Exception {
        ReceiveMessageResult receiveMessageResult = mock(ReceiveMessageResult.class);
        Message message = mock(Message.class);
        when(message.getBody()).thenReturn("{}");
        when(receiveMessageResult.getMessages()).thenReturn(Arrays.asList(message));
        when(mockAmazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveMessageResult);

        List<PolledMessage<TestMessage>> receivedMessages = queueServicePoller.poll();

        assertThat(receivedMessages).hasSize(1);

        when(mockAmazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(mock(ReceiveMessageResult.class));

        queueServicePoller.delete(receivedMessages.get(0));
        receivedMessages = queueServicePoller.poll();
        assertThat(receivedMessages).isEmpty();
    }

    @Test
    public void deleteBatchMessagesShouldWork() throws Exception {

        ReceiveMessageResult receiveMessageResult = mock(ReceiveMessageResult.class);
        Message message = mock(Message.class);
        when(message.getBody()).thenReturn("{}");
        when(receiveMessageResult.getMessages()).thenReturn(Arrays.asList(message, message));
        when(mockAmazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveMessageResult);


        List<PolledMessage<TestMessage>> receivedMessages = queueServicePoller.poll();

        assertEquals(2, receivedMessages.size());

        when(mockAmazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(mock(ReceiveMessageResult.class));

        queueServicePoller.delete(receivedMessages.get(0));
        queueServicePoller.delete(receivedMessages.get(1));
        receivedMessages = queueServicePoller.poll();
        assertEquals(0, receivedMessages.size());
    }
}
