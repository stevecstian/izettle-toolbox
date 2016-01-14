package com.izettle.messaging.queue;

import static java.util.Objects.requireNonNull;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.locks.StampedLock;

/**
 * File based FIFO queue.
 *
 * The task queue supports adding, peeking, removing and retrying tasks.
 *
 * New records are appended to the end of the file. The file grows dynamically and
 * has no size limits other than that of the file system.
 *
 * The file on disk has the layout of a fixed header and dynamically growing body
 * of records
 *
 * ------------
 * |  HEADER  |
 * ------------
 * |  RECORD  |
 * |  RECORD  |
 * |  RECORD  |
 * |  RECORD  |
 * ------------
 *
 * A record contains a static part and a dynamic part.
 *
 * ---------------------
 * |  HEADER  |  BODY  |
 * ---------------------
 *
 * Header is made up of:
 * 1 byte: Version
 * 1 long: Number of Records in file
 *
 * Record Header is made up of:
 * 1 byte: Tombstone
 * 1 int: Record Body Size
 * 1 int: Retries
 *
 * Record Body is made up of:
 * 1 int: Type size
 * byte[]: Type value
 * 1 int: Payload size
 * byte[]: Payload value
 *
 * As the BODY part of a record is dynamic, records will be different in size to each other.
 * It could look something like this.
 *
 * ---------------------
 * |  HEADER  |  BODY  |
 * --------------------------
 * |  HEADER  |     BODY    |
 * --------------------------
 * |  HEADER  | BODY |
 * -------------------
 *
 * Adding:
 * Adding a new entry writes a complete record to the end of the file.
 *
 *
 * Retry:
 * Retry writes an incremented integer to the records retry field.
 *
 *
 * Remove:
 * Remove writes a tombstone indicator to the tombstone field of the record. Peek will skip
 * tombstone records. A peek of 10 will always return 10 records if there are at least 10 not
 * tombstone records in the file.
 * The fact that remove does not remove the data from the file makes remove operations fast.
 * It also leaves data in the queue which if kept for too long has a possibility of slowing
 * down peek operations. In order to permanently delete data see the truncate() method.
 *
 *
 * Truncate:
 * Truncate removes data from the top of the queue until the first not tombstone record. This
 * implies that not all data is removed. If the first record is not a tombstone or if only record
 * 5-6 are tombstone then those tombstone records will not be removed. The queue has the
 * possibility of getting fragmented, it will still operate but may be slower on peek operations.
 * The implementation relies on the fact that the queue is FIFO and that any client will want to
 * consume the top messages from the queue and remove them as fast as possible.
 *
 * In the scenario below record 1 and 2 will be deleted from the file. Record 4 which is a tombstone will
 * not be deleted as it is not part of a consecutive sequence of tombstone records starting from the top
 * of the queue. Record 4 will be deleted once record number 3 has been removed.
 *
 * ---------------------------
 * | 1 | TOMBSTONE  |  BODY  |
 * ------------------------------
 * | 2 | TOMBSTONE  |  BODY     |
 * --------------------------------
 * | 3 |   ALIVE    |     BODY    |
 * --------------------------------
 * | 4 |   TOMBSTONE    | BODY |
 * -----------------------------
 *
 * In the scenario below no records will be deleted.
 *
 * --------------------------------
 * | 1 |   ALIVE    |     BODY    |
 * --------------------------------
 * | 2 |   ALIVE    | BODY |
 * ---------------------------
 * | 3 | TOMBSTONE  |  BODY  |
 * ------------------------------
 * | 4 | TOMBSTONE  |  BODY     |
 * ------------------------------
 * | 2 |   ALIVE    | BODY |
 * -------------------------
 *
 *
 * Thread safety.
 *
 * This class is not thread safe for multiple consumers or writers. It is thread safe for read operations
 * and add operations from two threads at the same time.
 *
 * The methods peek, retry, and remove are used together. A client first issues a peek where it gets a collection
 * of tasks that it either retries, removes or a combination of the two. As there are no transactions for those methods
 * the queue cannot know if there is a client waiting to do a retry or remove operation. If a client does a
 * remove or retry operation on a collection of tasks that was peeked before a truncate operation the offsets
 * of those tasks will have changed after the truncate has completed and any remove or retry operation will corrupt
 * the file. This is why a truncate operation must be called by the same thread as the one calling peek. In short
 * truncate may not be called in between peek and (retry or remove).
 *
 * An improvement would be to not expose the remove and retry methods, instead use a method peekInTx(int, TxFunc)
 * where TxFunc has arguments for a remover method and a retry method. This way we can assert that no method can write
 * corrupt data.
 *
 */
public class RandomAccessTaskQueue implements TaskQueue {

    private static final byte VERSION = 1;
    private static final int HEADER_SIZE = Byte.BYTES + Long.BYTES;
    private static final int RECORD_HEADER_SIZE = Byte.BYTES + Integer.BYTES;
    private static final byte TOMBSTONE = 1;
    private static final String UTF_8 = "UTF-8";

    private final RandomAccessFile raf;
    private final StampedLock truncateAndAddLock;
    private int currentRecordCnt;

    public RandomAccessTaskQueue(RandomAccessFile raf) {
        requireNonNull(raf);

        this.raf = raf;

        truncateAndAddLock = new StampedLock();

        updateHeader(0);
    }

    private void updateHeader(long additionalRecords) {
        try {
            raf.seek(0L);

            final ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
            buffer.put(VERSION);
            buffer.putLong(currentRecordCnt + additionalRecords);

            raf.write(buffer.array());

            currentRecordCnt += additionalRecords;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write header", e);
        }
    }

    @Override
    public int size() {
        return currentRecordCnt;
    }

    @Override
    public void add(Task task) {
        long stamp = truncateAndAddLock.writeLock();
        try {
            final long offset = raf.length();
            final byte[] typeBytes = task.getType().getBytes(UTF_8);
            final byte[] payloadBytes = task.getPayload().getBytes(UTF_8);

            final int taskByteSize = 3 * Integer.BYTES + typeBytes.length + payloadBytes.length;
            final int totalSize = RECORD_HEADER_SIZE + taskByteSize;

            final ByteBuffer buffer = ByteBuffer.allocate(totalSize);

            // Record header with tombstone and task size
            buffer.put((byte) 0);
            buffer.putInt(taskByteSize);

            buffer.putInt(0); // retries, always 0 for a new entry

            buffer.putInt(typeBytes.length);
            buffer.put(typeBytes);

            buffer.putInt(payloadBytes.length);
            buffer.put(payloadBytes);

            raf.seek(offset);
            raf.write(buffer.array());

            updateHeader(1);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to write task to file", e);
        } finally {
            truncateAndAddLock.unlockWrite(stamp);
        }
    }

    @Override
    public void addAll(Collection<? extends Task> tasks) {
        for (Task task : tasks) {
            add(task);
        }
    }

    @Override
    public Collection<QueuedTask> peek(int num) {
        try {
            long offset = HEADER_SIZE;

            raf.seek(offset);

            final int tasksToRead = size() > num ? num : size();

            final Collection<QueuedTask> tasks = new ArrayList<>((int) tasksToRead);

            int tasksRead = 0;
            while (tasksRead < tasksToRead) {
                final byte tombstoned = raf.readByte();
                final int totalSize = raf.readInt();

                if (tombstoned == TOMBSTONE) {
                    raf.skipBytes(totalSize);
                    continue;
                }

                final byte[] taskBytes = new byte[totalSize];

                raf.readFully(taskBytes);

                final ByteBuffer buffer = ByteBuffer.wrap(taskBytes);

                final int retry = buffer.getInt();
                final int typeSize = buffer.getInt();
                final byte[] typeBytes = new byte[typeSize];
                buffer.get(typeBytes);

                final int payloadSize = buffer.getInt();
                final byte[] payloadBytes = new byte[payloadSize];
                buffer.get(payloadBytes);

                tasks.add(
                    new QueuedTask(
                        offset,
                        new String(typeBytes, UTF_8),
                        new String(payloadBytes, UTF_8),
                        retry
                    )
                );

                offset = raf.getFilePointer();
                tasksRead++;
            }

            return tasks;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to peek into queue", e);
        }
    }

    @Override
    public void remove(Collection<QueuedTask> tasks) {
        try {
            for (QueuedTask task : tasks) {
                raf.seek(task.getID());
                raf.writeByte(TOMBSTONE);
            }

            updateHeader(-tasks.size());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to remove tasks", e);
        }
    }

    public void truncate() {
        final long stamp = truncateAndAddLock.writeLock();

        long startOffset = HEADER_SIZE;
        try {
            while (true) {
                raf.seek(startOffset);

                final byte tombstone = raf.readByte();
                final int recordSize = raf.readInt();

                if (tombstone != TOMBSTONE) {
                    raf.skipBytes(recordSize);
                    break;
                }

                startOffset += RECORD_HEADER_SIZE + recordSize;
            }
        } catch (EOFException e) {
            // Normal, we have read until end of file without finding any deleted records
            truncateAndAddLock.unlockWrite(stamp);
            return;
        } catch (IOException e) {
            truncateAndAddLock.unlockWrite(stamp);
            throw new IllegalStateException("Unable to truncate", e);
        }

        try {
            if (startOffset == raf.length()) {
                // All records have been deleted, truncate the entire file but keep the header part.
                raf.setLength(HEADER_SIZE);
            } else {
                // There are records after the startOffset. Delete the chunk between start and end and then
                // write what is after the startOffset beginning at the start offset and then set the file
                // size.
                final byte[] tail = new byte[(int) (raf.length() - startOffset)];
                raf.seek(startOffset);
                raf.readFully(tail);
                raf.seek(startOffset);
                raf.write(tail);
                raf.setLength(startOffset + tail.length);
            }
        } catch (IOException e) {
            // This is not allowed to happen here, it means that the read and write methods akt on a file
            // that has changed or there is a general coding error.
            throw new IllegalStateException("Unable to truncate", e);
        } finally {
            truncateAndAddLock.unlockWrite(stamp);
        }
    }

    @Override
    public void retry(Collection<QueuedTask> tasks) {
        try {
            for (QueuedTask task : tasks) {
                raf.seek(task.getID() + RECORD_HEADER_SIZE);
                raf.writeInt(task.getRetryCount() + 1);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to remove tasks", e);
        }
    }

    public static void main(String[] args) throws IOException {
        final File file = new File("sample.db");
        file.createNewFile();

        final RandomAccessTaskQueue rw = new RandomAccessTaskQueue(new RandomAccessFile(file, "rwd"));

        final long l = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            rw.add(new Task("hello", "moto_" + i));
        }

        System.out.println("l = " + (System.currentTimeMillis() - l));

        Collection<QueuedTask> peek = rw.peek(900);

        for (QueuedTask queuedTask : peek) {
            System.out.println("queuedTask.getPayload() = " + queuedTask.getPayload());
        }

        rw.remove(peek);
        rw.truncate();

        peek = rw.peek(100);

        for (QueuedTask queuedTask : peek) {
            System.out.println("REMOVE: queuedTask.getPayload() = " + queuedTask.getPayload());
        }
    }
}
