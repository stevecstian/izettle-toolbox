package com.izettle.jdbi;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

public class InstantArgumentFactory implements ArgumentFactory<Instant> {
    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof Instant;
    }

    @Override
    public Argument build(Class<?> expectedType, Instant value, StatementContext ctx) {
        return new DefaultInstantArgument(value);
    }

    private static class DefaultInstantArgument implements Argument {
        private final Instant value;

        private DefaultInstantArgument(Instant value) {
            this.value = value;
        }

        @Override
        public void apply(
            int position,
            PreparedStatement statement,
            StatementContext ctx
        ) throws SQLException {
            statement.setTimestamp(position, new Timestamp(value.toEpochMilli()));
        }
    }

}
