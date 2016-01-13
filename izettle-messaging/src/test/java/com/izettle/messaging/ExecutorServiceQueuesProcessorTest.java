package com.izettle.messaging;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;

public class ExecutorServiceQueuesProcessorTest {

    private final Consumer exceptionConsumer = mock(Consumer.class);
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private ExecutorServiceQueuesProcessor executorServiceQueuesProcessor;

    @Before
    public void setup() {
        executorServiceQueuesProcessor =
            new ExecutorServiceQueuesProcessor(executorService, exceptionConsumer, 100, TimeUnit.MILLISECONDS);
    }

    @Test
    public void itShouldPollAllQueueConsumersWhenPolling() throws MessagingException {
        final MessageQueueProcessor mq1 = mock(MessageQueueProcessor.class);
        final MessageQueueProcessor mq2 = mock(MessageQueueProcessor.class);
        final MessageQueueProcessor mq3 = mock(MessageQueueProcessor.class);

        executorServiceQueuesProcessor.add(mq1);
        executorServiceQueuesProcessor.add(mq2);
        executorServiceQueuesProcessor.add(mq3);

        executorServiceQueuesProcessor.poll();

        verify(mq1).poll();
        verify(mq2).poll();
        verify(mq3).poll();
    }

    @Test
    public void itShouldNotThrowExceptionOnPollWhenOneOfTheMessageQueueProcessorsThrowException()
        throws MessagingException {
        final MessageQueueProcessor mq1 = mock(MessageQueueProcessor.class);
        final MessageQueueProcessor mq2 = mock(MessageQueueProcessor.class);

        executorServiceQueuesProcessor.add(mq1);
        executorServiceQueuesProcessor.add(mq2);

        doThrow(RuntimeException.class).when(mq2).poll();

        executorServiceQueuesProcessor.poll();

        verify(mq1).poll();
        verify(mq1).poll();

        verify(exceptionConsumer).accept(any(RuntimeException.class));
    }

    @Test
    public void itShouldAwaitDoneWhenPollingTakesAWhileAndThisMethodWouldBlockIfItWouldNotHandleANotDoneTask()
        throws InterruptedException, MessagingException {
        executorService = mock(ExecutorService.class);
        executorServiceQueuesProcessor =
            new ExecutorServiceQueuesProcessor(executorService, exceptionConsumer, 100, TimeUnit.MILLISECONDS);

        final Future f1 = mock(Future.class);
        final Future f2 = mock(Future.class);

        when(f2.isDone()).thenReturn(true);
        when(f1.isDone()).thenReturn(false, false, false, true);

        when(executorService.invokeAll(anyCollection())).thenReturn(Arrays.asList(f1, f2));

        executorServiceQueuesProcessor.poll();
    }
}
