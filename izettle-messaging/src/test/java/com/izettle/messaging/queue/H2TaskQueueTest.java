package com.izettle.messaging.queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.izettle.java.UUIDFactory;
import com.izettle.messaging.MessagingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class H2TaskQueueTest {

    private H2TaskQueue publisher;
    private final RetryStrategy retryStrategy = mock(RetryStrategy.class);

    @Before
    public void setup() {
        publisher = new H2TaskQueue(
            H2ConnectionProvider.inMemory(UUIDFactory.createUUID4AsString()),
            new H2StatementManager(),
            retryStrategy
        );

        when(retryStrategy.decide(anyCollection())).then(a -> a.getArguments()[0]);
    }

    @After
    public void teardown() throws SQLException {
    }

    @Test
    public void itShouldEnqueueAndDequeueMessagesInInsertionOrder() throws MessagingException {
        final QueuedTask item1 = new QueuedTask(1, "SimpleMessage", "hello moto", 0);
        final QueuedTask item2 = new QueuedTask(2, "SomeMessage", "hello moto1", 0);
        final QueuedTask item3 = new QueuedTask(3, "This Is Sparta", "hello moto2", 0);

        publisher.add(item1);
        publisher.add(item2);
        publisher.add(item3);

        final List<QueuedTask> all = publisher.peek(3);

        assertThat(all.get(0)).isEqualToComparingFieldByField(item1);
        assertThat(all.get(1)).isEqualToComparingFieldByField(item2);
        assertThat(all.get(2)).isEqualToComparingFieldByField(item3);
    }

    @Test
    public void itShouldBulkEnqueueAndDequeueMessagesInInsertionOrder() throws MessagingException {
        final QueuedTask item1 = new QueuedTask(1, "SimpleMessage", "hello moto", 0);
        final QueuedTask item2 = new QueuedTask(2, "SomeMessage", "hello moto1", 0);
        final QueuedTask item3 = new QueuedTask(3, "This Is Sparta", "hello moto2", 0);

        publisher.addAll(Arrays.asList(item1, item2, item3));

        final List<QueuedTask> all = publisher.peek(3);

        assertThat(all.get(0)).isEqualToComparingFieldByField(item1);
        assertThat(all.get(1)).isEqualToComparingFieldByField(item2);
        assertThat(all.get(2)).isEqualToComparingFieldByField(item3);
    }

    @Test
    public void itShouldPeekOnlySpecifiedNumberOfItems() throws MessagingException {
        final QueuedTask item1 = new QueuedTask(1, "SimpleMessage", "hello moto", 0);
        final QueuedTask item2 = new QueuedTask(2, "SomeMessage", "hello moto1", 0);
        final QueuedTask item3 = new QueuedTask(3, "This Is Sparta", "hello moto2", 0);

        publisher.addAll(Arrays.asList(item1, item2, item3));

        final List<QueuedTask> all = publisher.peek(2);

        assertThat(all.get(0)).isEqualToComparingFieldByField(item1);
        assertThat(all.get(1)).isEqualToComparingFieldByField(item2);

        assertThat(all.size()).isEqualTo(2);
    }

    @Test
    public void itShouldNotRemoveItemsWhenPeeking() throws MessagingException {
        addTasks();

        // Peek once
        publisher.peek(2);
        // Peek twice
        final List<QueuedTask> all = publisher.peek(3);

        assertThat(all.size()).isEqualTo(3);
    }

    @Test
    public void itShouldRemoveMessagesWhenCallingRemove() {
        addTasks();

        List<QueuedTask> all = publisher.peek(3);
        publisher.remove(all);

        all = publisher.peek(3);
        assertThat(all.size()).isEqualTo(0);
    }

    @Test
    public void itShouldOnlyRemoveItemsSentToRemove() {
        final QueuedTask item1 = new QueuedTask(1, "SimpleMessage", "hello moto", 0);
        final QueuedTask item2 = new QueuedTask(2, "SomeMessage", "hello moto1", 0);
        final QueuedTask item3 = new QueuedTask(3, "This Is Sparta", "hello moto2", 0);

        publisher.addAll(Arrays.asList(item1, item2, item3));

        // Peek to get the list of items
        List<QueuedTask> all = publisher.peek(3);
        // remove item number 2 from the list of items to be removed.
        all.remove(1);
        // remove all but item number two
        publisher.remove(all);

        all = publisher.peek(3);
        assertThat(all.size()).isEqualTo(1);
        // item number 2 should still be in the queue as it was removed from the list sent to remove method
        assertThat(all.get(0)).isEqualToComparingFieldByField(item2);
    }

    @Test
    public void itShouldNotThrowExceptionWhenRemovingItemThatIsNotInTheQueue() {
        publisher.remove(Arrays.asList(new QueuedTask(1, "typish", "hello moto", 1)));
    }

    @Test
    public void itShouldReturnZeroElementsWhenPeeekingIntoEmptyQueue() {
        List<QueuedTask> all = publisher.peek(3);
        assertThat(all.size()).isEqualTo(0);
    }

    @Test(expected = NullPointerException.class)
    public void itShouldThrowExceptionWhenAddingNullTask() {
        Task t = null;
        publisher.add(t);
    }

    @Test(expected = NullPointerException.class)
    public void itShouldThrowExceptionWhenAddingNullTasks() {
        List<Task> t = null;
        publisher.addAll(t);
    }

    @Test(expected = IllegalStateException.class)
    public void itShouldNotAddAnyElementIfTheConnectionIsClosed() throws SQLException {
        Connection connection = mock(Connection.class);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        doThrow(new SQLException()).when(connection).commit();

        final Statement statement = mock(Statement.class);
        when(statement.execute(anyString())).thenReturn(true);
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt(0)).thenReturn(0);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.createStatement()).thenReturn(statement);

        publisher = new H2TaskQueue(() -> connection, new H2StatementManager(), retryStrategy);

        addTasks();
    }

    @Test
    public void itShouldIncreasePushbackCntWhenPushingBackTasks() {
        addTasks();

        List<QueuedTask> all = publisher.peek(3);
        publisher.retry(all);

        all = publisher.peek(3);

        assertThat(all.get(0).getRetryCount()).isEqualTo(1);
    }

    @Test
    public void itShouldIncreasePushbackCntWhenPushingBackTasksMultipleTimes() {
        addTasks();

        List<QueuedTask> all = publisher.peek(3);
        publisher.retry(all);
        all = publisher.peek(3);
        publisher.retry(all);
        all = publisher.peek(3);
        publisher.retry(all);
        all = publisher.peek(3);

        assertThat(all.get(1).getRetryCount()).isEqualTo(3);
    }

    @Test
    public void itShouldKeepOrderingWhenIncreasingRetryCount() {
        final QueuedTask item1 = new QueuedTask(1, "SimpleMessage", "hello moto", 0);
        final QueuedTask item2 = new QueuedTask(2, "SomeMessage", "hello moto1", 0);
        final QueuedTask item3 = new QueuedTask(3, "This Is Sparta", "hello moto2", 0);

        publisher.addAll(Arrays.asList(item1, item2, item3));

        List<QueuedTask> all = publisher.peek(3);
        publisher.retry(all);
        all = publisher.peek(3);
        publisher.retry(all);
        all = publisher.peek(3);
        publisher.retry(all);

        final QueuedTask item4 = new QueuedTask(4, "This Is Sparta", "hello moto2", 0);

        publisher.add(item4);

        all = publisher.peek(4);

        assertThat(all.get(0)).isEqualToComparingOnlyGivenFields(item1, "id");
        assertThat(all.get(1)).isEqualToComparingOnlyGivenFields(item2, "id");
        assertThat(all.get(2)).isEqualToComparingOnlyGivenFields(item3, "id");
        assertThat(all.get(3)).isEqualToComparingOnlyGivenFields(item4, "id");
    }

    @Test
    public void itShouldIncreaseRetryCount() {
        final QueuedTask item1 = new QueuedTask(1, "SimpleMessage", "hello moto", 0);
        final QueuedTask item2 = new QueuedTask(2, "SomeMessage", "hello moto1", 0);
        final QueuedTask item3 = new QueuedTask(3, "This Is Sparta", "hello moto2", 0);

        publisher.addAll(Arrays.asList(item1, item2, item3));

        List<QueuedTask> all = publisher.peek(3);
        publisher.retry(all);
        all = publisher.peek(1); // pushback item 1
        publisher.retry(all);
        all = publisher.peek(2); // pushback item 2, 3, 1
        publisher.retry(all);

        final QueuedTask item4 = new QueuedTask(4, "This Is Sparta", "hello moto2", 0);

        publisher.add(item4);

        all = publisher.peek(4);

        assertThat(all.get(0).getRetryCount()).isEqualTo(3);
        assertThat(all.get(1).getRetryCount()).isEqualTo(2);
        assertThat(all.get(2).getRetryCount()).isEqualTo(1);
    }

    @Test
    public void itShouldRestartIdentityWhenRemovingTheLastItemFromTheQueue() {
        addTasks();
        List<QueuedTask> all = publisher.peek(3);
        publisher.remove(all);

        assertThat(publisher.size()).isEqualTo(0);

        final QueuedTask item4 = new QueuedTask(1, "This Is Sparta", "hello moto2", 0);

        publisher.add(item4);

        all = publisher.peek(1);
        assertThat(all.get(0)).isEqualToComparingFieldByField(item4);
    }

    @Test
    public void itShouldIncreaseTheCountWhenAddingTasks() {
        addTasks();
        assertThat(publisher.size()).isEqualTo(3);
    }

    @Test
    public void itShouldNotDecreaseCountWhenPeeking() {
        addTasks();
        publisher.peek(10);
        assertThat(publisher.size()).isEqualTo(3);
    }

    @Test
    public void itShouldNotIncreaseTheCountWhenFailingToInsertTasksInQueue() throws SQLException {
        Connection connection = mock(Connection.class);

        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeUpdate()).thenThrow(new SQLException());

        final Statement statement = mock(Statement.class);
        when(statement.execute(anyString())).thenReturn(true);
        final ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getInt(0)).thenReturn(0);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.createStatement()).thenReturn(statement);

        publisher = new H2TaskQueue(() -> connection, new H2StatementManager(), retryStrategy);

        try {
            addTasks();
        } catch (Exception e) {
        }

        assertThat(publisher.size()).isEqualTo(0);
    }

    private void addTasks() {
        final QueuedTask item1 = new QueuedTask(1, "SimpleMessage", "hello moto", 0);
        final QueuedTask item2 = new QueuedTask(2, "SomeMessage", "hello moto1", 0);
        final QueuedTask item3 = new QueuedTask(3, "This Is Sparta", "hello moto2", 0);

        publisher.addAll(Arrays.asList(item1, item2, item3));
    }
}
