package com.izettle.messaging.queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;

public class DeadLetterQueueRetryStrategyTest {

    private final TaskQueue dlq = mock(TaskQueue.class);
    private DeadLetterQueueRetryStrategy strategy;

    @Before
    public void setup() {
       strategy = new DeadLetterQueueRetryStrategy(dlq, 10);
    }

    @Test
    public void itShouldRetryTasksIfTheyHaveNotExceededMaxRetries() {
        final List<QueuedTask> queuedTasks = new LinkedList<>();
        queuedTasks.add(new QueuedTask(1, "apa", "kaka", 0));
        queuedTasks.add(new QueuedTask(2, "apa", "kaka", 0));

        assertThat(strategy.decide(queuedTasks)).containsAll(queuedTasks);
    }

    @Test
    public void itShouldNotRetryTasksIfTheyHaveExceededMaxRetries() {
        final List<QueuedTask> queuedTasks = new LinkedList<>();
        queuedTasks.add(new QueuedTask(1, "apa", "kaka", 11));
        queuedTasks.add(new QueuedTask(2, "apa", "kaka", 11));

        assertThat(strategy.decide(queuedTasks)).doesNotContainAnyElementsOf(queuedTasks);
    }

    @Test
    public void itShouldOnlyRetryTasksThatHaveExceededRetryLimite() {
        final List<QueuedTask> queuedTasks = new LinkedList<>();
        final QueuedTask queuedTask = new QueuedTask(1, "apa", "kaka", 3);
        queuedTasks.add(queuedTask);
        queuedTasks.add(new QueuedTask(2, "apa", "kaka", 11));

        assertThat(strategy.decide(queuedTasks)).containsExactly(queuedTask);
    }

    @Test
    public void itShouldEnqueueTasksThatHaveExceededRetryLimitToDeadLetterQueue() {
        final List<QueuedTask> queuedTasks = new LinkedList<>();
        queuedTasks.add(new QueuedTask(1, "apa", "kaka", 11));
        queuedTasks.add(new QueuedTask(2, "apa", "kaka", 11));

        strategy.decide(queuedTasks);

        verify(dlq).addAll(
            Matchers.argThat(
                new ArgumentMatcher<Collection<? extends Task>>() {
                    @Override
                    public boolean matches(Object list) {
                        return ((List) list).size() == 2;
                    }
                }
            )
        );
    }
    @Test
    public void itShouldEnqueueTasksThatHaveExceededRetryLimitToDeadLetterQueueAndNotEnqueueTheOthers() {
        final List<QueuedTask> queuedTasks = new LinkedList<>();
        queuedTasks.add(new QueuedTask(1, "apa", "kaka", 2));
        queuedTasks.add(new QueuedTask(2, "apa", "kaka", 11));

        strategy.decide(queuedTasks);

        verify(dlq).addAll(Matchers.argThat(
                new ArgumentMatcher<Collection<? extends Task>>() {
                    @Override
                    public boolean matches(Object list) {
                        return ((List) list).size() == 1;
                    }
                }
            ));
    }
}
