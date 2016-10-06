package com.izettle.cassandra.migration;

import static java.util.Objects.requireNonNull;

import com.datastax.driver.core.PagingState;
import com.datastax.driver.mapping.annotations.Table;
import com.datastax.driver.mapping.annotations.Transient;
import java.time.Instant;
import java.util.Optional;

@Table(name = "migration")
public class MigrationStatus {

    private String name;
    private String pagingHash;
    private String sourceTableName;
    private String destinationTableName;
    private MigrationState migrationState;
    private Long rowsProcessed;
    private Integer fetchSize;
    private Instant started;
    private Instant finished;
    private boolean readOnly;

    public MigrationStatus(MigrationConfig config) {

        this.name = config.getName();
        this.sourceTableName = config.getSourceTableName();
        this.destinationTableName = config.getDestinationTableName();
        this.fetchSize = config.getFetchSize();
        this.migrationState = MigrationState.IDLE;
        this.rowsProcessed = 0L;
        this.started = Instant.now();
        this.readOnly = true; //Force to always do a dry run.
    }

    public MigrationStatus setPagingHash(final String pagingHash) {
        this.pagingHash = requireNonNull(pagingHash, "pagingHash must not be null");
        return this;
    }

    public MigrationStatus setCompleted() {
        this.migrationState = MigrationState.COMPLETED;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getPagingHash() {
        return pagingHash;
    }

    @Transient
    public Optional<PagingState> getPagingState() {
        return Optional.ofNullable(pagingHash).map(PagingState::fromString);
    }

    public MigrationState getMigrationState() {
        return migrationState;
    }

    public Instant getStarted() {
        return started;
    }

    public Instant getFinished() {
        return finished;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public Long getRowsProcessed() {
        return rowsProcessed;
    }

    public String getSourceTableName() {
        return sourceTableName;
    }

    public String getDestinationTableName() {
        return destinationTableName;
    }

    public Integer getFetchSize() {
        return fetchSize;
    }
}
