package com.izettle.messaging.queue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.izettle.messaging.MessagePublisher;
import com.izettle.messaging.MessagingException;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;

public class TaskQueueMessageRePublisherTest {

    private final TaskQueue taskQueue = mock(TaskQueue.class);
    private final TaskQueue dlq = mock(TaskQueue.class);
    private final MessagePublisher messagePublisher = mock(MessagePublisher.class);
    private final Consumer exceptionConsumer = mock(Consumer.class);
    private final TaskSerializationConverter<String> taskSerializationConverter = mock(TaskSerializationConverter.class);
    private TaskQueueMessageRePublisher<String> rePublisher;

    @Before
    public void setup() {
        rePublisher = new TaskQueueMessageRePublisher<>(
            taskQueue,
            10,
            dlq,
            10,
            messagePublisher,
            "apakaka",
            taskSerializationConverter,
            exceptionConsumer
        );
    }

    @Test
    public void itShouldPeekAndPublishToMessagePublisherWhenPolling() throws MessagingException,
        IOException {
        final Collection<QueuedTask> tasks = mock(Collection.class);
        final Collection<String> deserializedTasks = mock(Collection.class);

        when(taskQueue.peek(anyInt())).thenReturn(tasks);
        when(taskSerializationConverter.revert(tasks)).thenReturn(deserializedTasks);

        rePublisher.poll();

        verify(messagePublisher).postBatch(deserializedTasks, "apakaka");
        verify(taskQueue).remove(tasks);
    }

    @Test
    public void itShouldPeekAndRetryMessagesWhenDestinationConsumerCannotHandleMessages() throws MessagingException {
        final Collection<QueuedTask> tasks = mock(Collection.class);

        when(taskQueue.peek(anyInt())).thenReturn(tasks);
        doThrow(MessagingException.class).when(messagePublisher).postBatch(anyCollection(), anyString());

        try {
            rePublisher.poll();
        } catch (MessagingException e) {
        }

        verify(taskQueue, never()).remove(anyCollection());
        verify(taskQueue).retry(tasks);
    }

    @Test
    public void itShouldAlternateBetweenNormalQueueAndDeadLetterQueue() throws MessagingException {
        for (int i = 0; i < 30; i++) {
            rePublisher.poll();
        }

        verify(taskQueue, times(27)).peek(10);
        verify(dlq, times(3)).peek(10);
    }

    @Test
    public void itShouldGiveExceptionToExceptionHandlerWhenQueueCannotBePeeked() throws MessagingException {
        when(taskQueue.peek(anyInt())).thenThrow(IllegalStateException.class);

        rePublisher.poll();

        verify(exceptionConsumer).accept(any(IllegalStateException.class));
    }
}
