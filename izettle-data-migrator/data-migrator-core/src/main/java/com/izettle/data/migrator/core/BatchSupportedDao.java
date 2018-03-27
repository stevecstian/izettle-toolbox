package com.izettle.data.migrator.core;

import java.util.List;
import java.util.function.Function;

/**
 * A DAO that can be used for batch operations.
 *
 * It must be Thread-safe because batch operations are typically run in multiple Threads.
 * @param <DTO> POJO representing the objects managed by this DAO.
 */
public interface BatchSupportedDao<DTO> {

    /**
     * Process all records in the database in batches.
     * @param batchSize size of each batch
     * @param consumer action to perform on each batch. Return true to continue, false to stop.
     */
    void forEachBatch(int batchSize, Function<List<DTO>, Boolean> consumer);

    /**
     * Check whether the given DTO already exists in the database.
     *
     * @param dto DTO to check
     * @return true if the DTO is in the database, false otherwise
     */
    boolean exists(DTO dto);

    /**
     * Stop performing one-by-one migrations if this DAO supports it.
     *
     * This method should not have any effect on the implementation of {@link #forEachBatch(int, Function)},
     * it should only stop performing one-by-one migrations if it implements that functionality.
     */
    void stopMigration();

}
