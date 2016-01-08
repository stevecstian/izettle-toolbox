package com.izettle.messaging.queue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.izettle.messaging.MessagePublisher;
import com.izettle.messaging.MessagingException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

public class TaskQueueBackedMessagePublisherTest {

    private final TaskQueue taskQueue = mock(H2TaskQueue.class);
    private final MessagePublisherTaskFactory taskFactory = mock(MessagePublisherTaskFactory.class);
    private final MessagePublisher destination = mock(MessagePublisher.class);
    private final Consumer<Exception> exceptionHandler = (Consumer<Exception>) mock(Consumer.class);
    private final MessagingException messagingException = new MessagingException("event wrongly");

    private TaskQueueBackedMessagePublisher publisher;

    @Before
    public void setup() throws MessagingException {
        doThrow(messagingException).when(destination).post(any(SimpleMessage.class), anyString());

        publisher = new TaskQueueBackedMessagePublisher(destination, taskQueue, taskFactory, exceptionHandler);
    }

    @Test
    public void itShouldAddMessageToQueueWhenPosting() throws MessagingException, JsonProcessingException {
        final SimpleMessage message = new SimpleMessage(10);
        final QueuedTask task = new QueuedTask(1L, "event", "some payload", 0);

        when(taskFactory.create(message, "event")).thenReturn(task);

        publisher.post(message, "event");

        verify(taskQueue).add(task);
    }

    @Test(expected = MessagingException.class)
    public void itShouldThrowMessagingExceptionWhenSerializeDoesNotWork()
        throws JsonProcessingException, MessagingException {
        when(taskFactory.create(any(Object.class), anyString())).thenThrow(JsonProcessingException.class);

        publisher.post(new SimpleMessage(10), "event");

        verify(taskQueue, never()).add(any(Task.class));
    }

    @Test
    public void itShouldAddMessagesToQueueWhenPosting() throws MessagingException, JsonProcessingException {
        final List<SimpleMessage> messages = Arrays.asList(new SimpleMessage(10));
        final List<Task> tasks = Arrays.asList(new QueuedTask(1L, "event", "some payload", 0));

        when(taskFactory.create(messages, "event")).thenReturn(tasks);

        publisher.postBatch(messages, "event");

        verify(taskQueue).add(tasks);
    }

    @Test(expected = MessagingException.class)
    public void itShouldThrowMessagingExceptionWhenSerializeDoesNotWorkWhenPostingBatch()
        throws JsonProcessingException, MessagingException {
        when(taskFactory.create(any(Object.class), anyString())).thenThrow(JsonProcessingException.class);

        publisher.post(Arrays.asList(new SimpleMessage(10)), "event");

        verify(taskQueue, never()).add(Matchers.<List<Task>>any());
    }

    @Test
    public void itShouldNotifyExceptionConsumerWhenTheDestinationQueueCouldNotHandleMessage()
        throws JsonProcessingException, MessagingException {
        final SimpleMessage message = new SimpleMessage(10);
        final QueuedTask task = new QueuedTask(1L, "event", "some payload", 0);

        when(taskFactory.create(message, "event")).thenReturn(task);

        publisher.post(message, "event");

        verify(exceptionHandler).accept(messagingException);
    }

    private class SimpleMessage {
        private int amount;

        public SimpleMessage(int amount) {
            this.amount = amount;
        }

        public int getAmount() {
            return amount;
        }
    }
}
