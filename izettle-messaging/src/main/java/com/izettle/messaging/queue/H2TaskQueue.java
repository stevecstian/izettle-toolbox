package com.izettle.messaging.queue;

import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * H2Queue implements a queue with H2 as backing persistence. Choose to get a in memory Queue by
 * using a URL like jdbc:h2:mem:test where test is the name of the in memory queue. For a file based
 * queue use a URL like jdbc:h2:~/test where ~/test is the db file placed in the home directory of the
 * executing user.
 *
 * The Queue is a fifo queue with the added extra that the client has to tell the queue to remove the items
 * that it received. This way the client is in control over which items in the queue it was able to process.
 *
 * NOTE: This class is in its current state not thread safe for clients to peek at the same time, they may
 * receive the same tasks.
 */
public class H2TaskQueue implements TaskQueue {

    private final Supplier<Connection> connectionSupplier;
    private final StatementManager stmtManager;
    private final PushbackStrategy pushbackStrategy;
    private long queueCnt;

    public H2TaskQueue(
        Supplier<Connection> connectionSupplier,
        StatementManager stmtManager,
        PushbackStrategy pushbackStrategy
    ) {
        requireNonNull(pushbackStrategy);
        requireNonNull(connectionSupplier);
        requireNonNull(stmtManager);

        this.pushbackStrategy = pushbackStrategy;
        this.stmtManager = stmtManager;
        this.connectionSupplier = connectionSupplier;

        initialize();
    }

    private void initialize() {
        try {
            final Connection connection = connectionSupplier.get();

            connection.createStatement().execute(stmtManager.getCreateDatabaseStmt());

            final ResultSet resultSet = connection.createStatement().executeQuery(stmtManager.getCountStmt());

            if (resultSet.next()) {
                queueCnt = resultSet.getLong(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to setup queue", e);
        }
    }

    /**
     * Get the sie of the queue
     * @return
     */
    public long size() {
        return queueCnt;
    }

    /**
     * Add queue item to queue. The item will be inserted at the end of the queue.
     * @param task must not be null
     */
    @Override
    public void add(Task task) {
        requireNonNull(task);

        add(Collections.singletonList(task));
    }

    /**
     * Add queue items to queue. Items are added to the queue in batch. If one of the items cannot be inserted
     * then no items are inserted.
     * @param tasks
     * @throws IllegalStateException if task cannot be added to queue.
     */
    @Override
    public void add(Collection<Task> tasks) {
        requireNonNull(tasks);

        final Connection connection = connectionSupplier.get();

        try (PreparedStatement insertStmt = connection.prepareStatement(stmtManager.getInsertStmt())) {
            connection.setAutoCommit(false);

            for (Task task : tasks) {
                insertOne(task, insertStmt);
            }

            connection.commit();

            queueCnt += tasks.size();
        } catch (SQLException e) {
            final StringJoiner joiner = new StringJoiner("\n");
            tasks.forEach(
                i -> {
                    joiner.add(i.getType());
                    joiner.add(i.getPayload());
                }
            );

            throw new IllegalStateException(
                String.format("Unable to add tasks to queue: [%s]", joiner.toString()),
                e
            );
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
            }
        }
    }

    private void insertOne(Task task, PreparedStatement insertStmt)
        throws SQLException {
        insertStmt.setInt(1, 0); // push back cnt
        insertStmt.setString(2, task.getType());
        insertStmt.setString(3, task.getPayload());
        insertStmt.executeUpdate();
    }

    /**
     * Retrieves without removing the head of this queue. Retrieves a max number of items defined by num.
     * If less items are in the queue then the num then all items are returned. If more items than num
     * are in the queue then exactly num tasks will be returned.
     * @param num The number of tasks to retrieve.
     * @return
     */
    @Override
    public List<QueuedTask> peek(int num) {
        final List<QueuedTask> payloads = new LinkedList<>();

        try (PreparedStatement stmt = connectionSupplier.get().prepareStatement(stmtManager.getSelectStmt())) {
            stmt.setInt(1, num);

            final ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                final long id = resultSet.getLong(1);
                final int pushbackCnt = resultSet.getInt(2);
                final String eventName = resultSet.getString(3);
                final String payload = resultSet.getString(4);

                payloads.add(new QueuedTask(id, eventName, payload, pushbackCnt));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to peek into queue", e);
        }

        return payloads;
    }

    /**
     * Remove queue tasks from the queue that exist in the queue. If the tasks in the list are not in the queue
     * then nothing is done.
     *
     * @param tasks The tasks to remove
     */
    @Override
    public void remove(Collection<QueuedTask> tasks) {
        final Connection connection = connectionSupplier.get();
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(stmtManager.getDeleteStmt())) {
                for (QueuedTask item : tasks) {
                    stmt.setLong(1, item.getID());
                    stmt.executeUpdate();
                }
            }

            connection.commit();

            if (queueCnt - tasks.size() == 0) {
                // Restart identity if the queue is empty.
                connection.createStatement().execute(stmtManager.getResetIdStmt());
            }

            queueCnt -= tasks.size();
        } catch (SQLException e) {
            final StringJoiner joiner = new StringJoiner("\n");
            tasks.forEach(
                i -> {
                    joiner.add(i.getType());
                    joiner.add(i.getPayload());
                }
            );

            throw new IllegalStateException(
                String.format(
                    "Unable to remove tasks from queue: [%s]",
                    joiner.toString()
                ),
                e
            );
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
            }
        }
    }

    /**
     * Push back will push back tasks that are early in the queue backwards. Items that have been pushed backed
     * multiple times will come after tasks that have been pushed back less when performing a peek.
     * @param tasks
     */
    @Override
    public void pushBack(Collection<QueuedTask> tasks) {
        requireNonNull(tasks);

        final Collection<QueuedTask> tasksToPushBack = pushbackStrategy.decide(tasks);
        final Collection<QueuedTask> tasksToRemove = tasks
            .stream()
            .filter(t -> !tasksToPushBack.contains(t))
            .collect(Collectors.toList());

        final Connection connection = connectionSupplier.get();
        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(stmtManager.getIncPushbackStmt())) {
                for (QueuedTask item : tasksToPushBack) {
                    stmt.setInt(1, item.getPushbackCount() + 1);
                    stmt.setLong(2, item.getID());
                    stmt.executeUpdate();
                }
            }

            connection.commit();
        } catch (SQLException e) {
            final StringJoiner joiner = new StringJoiner("\n");
            tasksToPushBack.forEach(
                i -> {
                    joiner.add(i.getType());
                    joiner.add(i.getPayload());
                }
            );

            throw new IllegalStateException(
                String.format(
                    "Unable to pushback task from queue: [%s]",
                    joiner.toString()
                ),
                e
            );
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
            }
        }

        try {
            remove(tasksToRemove);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to remove tasks from queue that should not be pushed back", e);
        }
    }
}
