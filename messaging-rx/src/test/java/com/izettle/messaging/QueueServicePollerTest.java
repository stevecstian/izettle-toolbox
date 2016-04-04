package com.izettle.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;

public class QueueServicePollerTest {

    private MessageQueueConsumer<TestMessage> queueServicePoller;
    private AmazonSQSAsync mockAmazonSQS = mock(AmazonSQSAsync.class);

    @Before
    public final void before() throws Exception {
        queueServicePoller =
            QueueServicePoller.nonEncryptedMessageQueueConsumer(TestMessage.class, "queueUrl", mockAmazonSQS);
    }

    @Test
    public void pollAndDeleteMessageShouldWork() throws Exception {
        ReceiveMessageResult receiveMessageResult = mock(ReceiveMessageResult.class);
        Message message = mock(Message.class);
        when(message.getBody()).thenReturn("{}");
        when(receiveMessageResult.getMessages()).thenReturn(Collections.singletonList(message));
        when(mockAmazonSQS.receiveMessageAsync(any(ReceiveMessageRequest.class))).thenReturn(CompletableFuture
            .completedFuture(
                receiveMessageResult));

        List<PolledMessage<TestMessage>> receivedMessages1 = queueServicePoller.poll().toBlocking().first();

        assertThat(receivedMessages1).hasSize(1);

        when(mockAmazonSQS.receiveMessageAsync(any(ReceiveMessageRequest.class))).thenReturn(CompletableFuture
            .completedFuture(
                mock(ReceiveMessageResult.class)));

        queueServicePoller.delete(receivedMessages1.get(0));
        List<PolledMessage<TestMessage>> receivedMessages2 = queueServicePoller.poll().toBlocking().first();
        assertThat(receivedMessages2).isEmpty();
    }

    @Test
    public void deleteBatchMessagesShouldWork() throws Exception {

        ReceiveMessageResult receiveMessageResult = mock(ReceiveMessageResult.class);
        Message message = mock(Message.class);
        when(message.getBody()).thenReturn("{}");
        when(receiveMessageResult.getMessages()).thenReturn(Arrays.asList(message, message));
        when(mockAmazonSQS.receiveMessageAsync(any(ReceiveMessageRequest.class))).thenReturn(CompletableFuture
            .completedFuture(
                receiveMessageResult));

        Observable<List<PolledMessage<TestMessage>>> futureMessages = queueServicePoller.poll();
        TestSubscriber<List<PolledMessage<TestMessage>>> testSubscriber = new TestSubscriber<>();
        List<PolledMessage<TestMessage>> polledMessages = futureMessages.toBlocking().single();

        futureMessages.subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertValueCount(1);
        assertEquals(2, polledMessages.size());

        when(mockAmazonSQS.receiveMessageAsync(any(ReceiveMessageRequest.class))).thenReturn(CompletableFuture
            .completedFuture(
                mock(ReceiveMessageResult.class)));

        queueServicePoller.delete(polledMessages.get(0));
        queueServicePoller.delete(polledMessages.get(1));
        List<PolledMessage<TestMessage>> receivedMessages2 = queueServicePoller.poll().toBlocking().first();
        assertEquals(0, receivedMessages2.size());
    }
}
