package com.izettle.data.migrator.core;

import java.util.Optional;

/**
 * This interface provides a way for DAOs to migrate Objects of type {@code FromData} to a new database or table
 * specified by the {@code ToExecute} type.
 *
 * Users of this interface should only call the {@link DataMigrator#migrate(Object)} method.
 *
 * @param <FromData> type of objects to be migrated
 * @param <ToExecute> action that migrates an object
 */
public interface DataMigrator<FromData, ToExecute> {

    /**
     * Transform object being migrated into a database statement that when executed, performs the actual migration.
     *
     * Users of this interface should call {@link DataMigrator#migrate(Object)} instead of this method.
     *
     * Returning an empty Optional causes the item to not be migrated.
     *
     * @param from object to be migrated
     * @return statements that can be executed to perform the migration, or empty to ignore the entry
     */
    Optional<ToExecute> transform(FromData from);

    /**
     * Execute the migration statement asynchronously.
     *
     * Users of this interface should call {@link DataMigrator#migrate(Object)} instead of this method.
     *
     * @param toExecute database statement that performs the migration of a single object
     */
    void executeAsync(ToExecute toExecute);

    /**
     * Execute the migration statement.
     *
     * Users of this interface should call {@link DataMigrator#migrate(Object)} instead of this method.
     *
     * @param toExecute database statement that performs the migration of a single object
     * @throws InterruptedException if the current Thread is interrupted while executing the statement
     */
    void execute(ToExecute toExecute) throws InterruptedException;

    /**
     * @param from record to check
     * @return true if the given record has been migrated successfully, false otherwise
     */
    boolean isMigrated(FromData from);

    /**
     * Migrate the given object.
     *
     * @param from object to be migrated
     * @throws InterruptedException if the current Thread is interrupted while executing the migration
     */
    default void migrate(FromData from) throws InterruptedException {
        transform(from).ifPresent(this::executeAsync);
    }

}
