package com.izettle.cassandra;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * A utility to handle time series in Cassandra.
 *
 * Every time series has a unique key and consists of events in time. An event is a piece of data with an UUID and time
 * associated with it.
 *
 * @param <K> Type of time series key.
 */
public class TimeSeries<K> {

    private final Keyspace keyspace;
    private final ColumnFamily<K, UUID> columnFamily;

    public TimeSeries(Keyspace keyspace, ColumnFamily<K, UUID> columnFamily) {
        this.keyspace = keyspace;
        this.columnFamily = columnFamily;
    }

    /**
     * Adds event to a time series.
     *
     * @param key Time series key.
     * @param uuid Event UUID.
     * @param date Event time.
     * @param value Event data.
     * @throws ConnectionException Failed to store event in Cassandra.
     */
    public void add(K key, UUID uuid, Date date, String value) throws ConnectionException {
        MutationBatch mutationBatch = keyspace.prepareMutationBatch();

        mutationBatch
                .withRow(columnFamily, key)
                .putColumn(DeterministicTimeUUIDFactory.create(uuid, date), value);

        mutationBatch.execute();
    }

    /**
     * Adds event to a time series.
     *
     * @param key Time series key.
     * @param eventString Event string. (Instead of seed UUID)
     * @param date Event time.
     * @param value Event data.
     * @throws ConnectionException Failed to store event in Cassandra.
     */
    public void add(K key, String eventString, Date date, String value) throws ConnectionException {
        MutationBatch mutationBatch = keyspace.prepareMutationBatch();

        mutationBatch
                .withRow(columnFamily, key)
                .putColumn(DeterministicTimeUUIDFactory.create(eventString, date), value);

        mutationBatch.execute();
    }

    /**
     * Get events for a specific time period.
     *
     * @param key Time series key.
     * @param begin From time (inclusive).
     * @param end To time (exclusive).
     * @param reversed If true, the order of the results will be reversed.
     * @param count Max number of events to return.
     * @return Time series events.
     * @throws ConnectionException Failed to retrieve time series events from Cassandra.
     */
    public List<String> get(K key, Date begin, Date end, boolean reversed, int count) throws ConnectionException {

        List<String> events = new ArrayList<>();

        OperationResult<ColumnList<UUID>> operationResult = keyspace
                .prepareQuery(columnFamily)
                .getKey(key)
                .withColumnRange(
                        DeterministicTimeUUIDFactory.createFirst(begin),
                        DeterministicTimeUUIDFactory.createLast(end),
                        reversed,
                        count
                )
                .execute();

        ColumnList<UUID> result = operationResult.getResult();

        for (Column<UUID> column : result) {
            if (column.hasValue()) {
                events.add(column.getStringValue());
            }
        }

        return events;
    }
}
