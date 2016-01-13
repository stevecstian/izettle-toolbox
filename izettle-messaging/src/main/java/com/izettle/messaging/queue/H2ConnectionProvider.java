package com.izettle.messaging.queue;

import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import org.h2.jdbcx.JdbcConnectionPool;

public class H2ConnectionProvider {

    public static Supplier<Connection> inMemory(String name) {
        try {
            Class.forName("org.h2.Driver");
            final JdbcConnectionPool jdbcConnectionPool =
                JdbcConnectionPool.create("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=0", "", "");
            return () -> {
                try {
                    return jdbcConnectionPool.getConnection();
                } catch (SQLException e) {
                    throw new IllegalStateException("Unable to acquire connection to H2 database");
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Supplier<Connection> file(String absolutePath) {
        try {
            Class.forName("org.h2.Driver");
            final JdbcConnectionPool jdbcConnectionPool =
                JdbcConnectionPool.create(
                    "jdbc:h2:split:"
                        + Paths.get(absolutePath).toAbsolutePath().toString()
                        + ";DB_CLOSE_DELAY=0;FILE_LOCK=NO", "", "");
            return () -> {
                try {
                    return jdbcConnectionPool.getConnection();
                } catch (SQLException e) {
                    throw new IllegalStateException("Unable to acquire connection to H2 database");
                }
            };
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
