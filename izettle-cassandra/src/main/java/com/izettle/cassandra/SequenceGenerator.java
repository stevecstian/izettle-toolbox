package com.izettle.cassandra;

import static com.izettle.java.ValueChecks.anyEmpty;

import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnMap;
import com.netflix.astyanax.recipes.locks.ColumnPrefixDistributedRowLock;
import com.netflix.astyanax.retry.BoundedExponentialBackoff;
import java.util.concurrent.TimeUnit;

/**
 * A sequence generator that generates a monotonically increasing sequence. The state is persisted in a given Cassandra
 * column family.
 * The sequence generator handles multiple sequences, each sequence is identified by a key represented as a string.
 */
public class SequenceGenerator<K> {

	private final static String COLUMN_NAME = "sequence";
	private final static BoundedExponentialBackoff RETRY_POLICY = new BoundedExponentialBackoff(10, 1000, 10);
	private final static long INITIAL_SEQUENCE_VALUE = 0L;

	private final Keyspace keyspace;
	private final ColumnFamily<K, String> columnFamily;

	/**
	 * Creates a sequence generator persisted in the given Cassandra column family.
	 *
	 * @param keyspace Keyspace of the column family.
	 * @param columnFamily Column family.
	 */
	public SequenceGenerator(Keyspace keyspace, ColumnFamily<K, String> columnFamily) {
		this.keyspace = keyspace;
		this.columnFamily = columnFamily;
	}

	/**
	 * Increments the sequence and returns the value.
	 *
	 * @param sequenceKey Sequence key.
	 * @return Sequence value.
	 * @throws SequenceGeneratorException Failed to update the sequence.
	 */
	public long incrementAndGet(K sequenceKey) throws SequenceGeneratorException {

		ColumnPrefixDistributedRowLock<K> lock =
				new ColumnPrefixDistributedRowLock<>(keyspace, columnFamily, sequenceKey)
						.withBackoff(RETRY_POLICY)
						.expireLockAfter(5, TimeUnit.SECONDS);

		try {
			ColumnMap<String> columns = lock.acquireLockAndReadRow();

			long nextSequenceNumber = columns.getLong(COLUMN_NAME, INITIAL_SEQUENCE_VALUE) + 1;

			MutationBatch mutationBatch = keyspace.prepareMutationBatch();

			mutationBatch
					.withRow(columnFamily, sequenceKey)
					.putColumn(COLUMN_NAME, nextSequenceNumber);

			lock.releaseWithMutation(mutationBatch);

			return nextSequenceNumber;
		} catch (Exception e) {
			try {
				lock.release();
			} catch (Exception e1) {
				throw new SequenceGeneratorException("Failed to release lock after exception.", e);
			}

			throw new SequenceGeneratorException("Failed to increment sequence!", e);
		}
	}

	/**
	 * Resets the sequence back to its initial value.
	 *
	 * @param sequenceKey Sequence key.
	 * @throws SequenceGeneratorException Failed to reset sequence.
	 */
	public void reset(K sequenceKey) throws SequenceGeneratorException {
		try {
			reset(sequenceKey, INITIAL_SEQUENCE_VALUE);
		} catch (SequenceGeneratorException e) {
			throw new SequenceGeneratorException("Failed to reset sequence!", e);
		}
	}

	/**
	 * Resets the sequence based on a given initial value.
	 *
	 * @param sequenceKey Sequence key.
	 * @param initialSequenceValue Initial sequence number.
	 * @throws SequenceGeneratorException Failed to create the sequence.
	 */
	public void reset(K sequenceKey, Long initialSequenceValue) throws SequenceGeneratorException {
		if (anyEmpty(sequenceKey, initialSequenceValue)) {
			throw new SequenceGeneratorException("Can't create a sequence without a sequence key or an initial value");
		}

		ColumnPrefixDistributedRowLock<K> lock =
				new ColumnPrefixDistributedRowLock<>(keyspace, columnFamily, sequenceKey)
						.withBackoff(RETRY_POLICY)
						.expireLockAfter(5, TimeUnit.SECONDS);

		try {
			lock.acquire();

			MutationBatch mutationBatch = keyspace.prepareMutationBatch();

			mutationBatch
					.withRow(columnFamily, sequenceKey)
					.putColumn(COLUMN_NAME, initialSequenceValue);

			lock.releaseWithMutation(mutationBatch);

		} catch (Exception e) {
			try {
				lock.release();
			} catch (Exception e1) {
				throw new SequenceGeneratorException("Failed to release lock after exception.", e);
			}

			throw new SequenceGeneratorException("Failed to create sequence!", e);
		}

	}
}
