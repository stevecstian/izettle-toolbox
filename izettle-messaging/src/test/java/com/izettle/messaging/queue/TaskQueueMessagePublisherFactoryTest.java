package com.izettle.messaging.queue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.izettle.messaging.ExecutorServiceQueuesProcessor;
import com.izettle.messaging.MessagePublisher;
import com.izettle.messaging.MessagingException;
import com.izettle.messaging.serialization.DefaultMessageSerializer;
import com.izettle.messaging.serialization.MessageDeserializer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;

public class TaskQueueMessagePublisherFactoryTest {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ExecutorServiceQueuesProcessor executorServiceQueuesProcessor = new ExecutorServiceQueuesProcessor(
        executorService,
        Throwable::printStackTrace,
        100,
        TimeUnit.MILLISECONDS
    );

    private TaskQueueMessagePublisherFactory publisherFactory;

    @Before
    public void setUp() throws Exception {
        publisherFactory =
            new TaskQueueMessagePublisherFactory(
                "",
                new H2TaskQueueFactory(H2ConnectionProvider::inMemory),
                executorServiceQueuesProcessor
            );
    }

    @Test
    public void itShouldCreateWorkingMessagePublisherWithPollingRePublisher() throws Exception {
        final MessagePublisher origin = mock(MessagePublisher.class);

        final MessagePublisher messagePublisher = publisherFactory.create(
            "important event",
            origin,
            new DefaultMessageSerializer(),
            new MessageDeserializer<>(SimpleMessage.class),
            Throwable::printStackTrace,
            10,
            10,
            10_000,
            10
            );

        doThrow(MessagingException.class).doNothing().when(origin).post(any(), anyString());
        messagePublisher.post(new SimpleMessage(), "important event");

        // First try where we go directly on the origin message publisher. It should be put on queue because of the
        // throw
        verify(origin).post(any(SimpleMessage.class), anyString());

        // Now poll one round of the queue re-publisher
        executorServiceQueuesProcessor.poll();

        verify(origin).postBatch(anyCollectionOf(SimpleMessage.class), anyString());
    }
}
