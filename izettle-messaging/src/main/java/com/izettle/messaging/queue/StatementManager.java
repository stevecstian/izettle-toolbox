package com.izettle.messaging.queue;

public class StatementManager {

    private static final String CREATE_DATABASE_STMT = ""
        + "CREATE TABLE IF NOT EXISTS queue "
        + "("
        + "id IDENTITY PRIMARY KEY"
        + ", pushback_cnt BIGINT"
        + ", type VARCHAR(255)"
        + ", payload CLOB"
        + ")";

    private static final String INSERT_STMT = ""
        + "INSERT INTO queue"
        + "("
        + "  pushback_cnt"
        + "  , type"
        + "  , payload"
        + ") "
        + "VALUES "
        + "("
        + "?"
        + ", ?"
        + ", ?"
        + ")";

    private static final String SELECT_STMT = ""
        + "SELECT "
        + "id"
        + ", pushback_cnt"
        + ", type"
        + ", payload "
        + "FROM queue"
        + " ORDER BY pushback_cnt ASC, id ASC"
        + " LIMIT ?";

    private static final String DELETE_STMT = ""
        + "DELETE FROM queue"
        + " WHERE id = ?";

    private static final String INC_PUSHBACK_STMT = ""
        + "UPDATE queue SET pushback_cnt = ?"
        + " WHERE id = ?";

    private static final String RESET_ID_STMT = ""
        + "ALTER TABLE queue"
        + " ALTER COLUMN id RESTART WITH 1";

    private static final String COUNT_STMT = ""
        + "SELECT count(1) FROM queue";

    public String getCreateDatabaseStmt() {
        return CREATE_DATABASE_STMT;
    }

    public String getInsertStmt() {
        return INSERT_STMT;
    }

    public String getSelectStmt() {
        return SELECT_STMT;
    }

    public String getDeleteStmt() {
        return DELETE_STMT;
    }

    public String getIncPushbackStmt() {
        return INC_PUSHBACK_STMT;
    }

    public String getResetIdStmt() {
        return RESET_ID_STMT;
    }

    public String getCountStmt() {
        return COUNT_STMT;
    }
}
