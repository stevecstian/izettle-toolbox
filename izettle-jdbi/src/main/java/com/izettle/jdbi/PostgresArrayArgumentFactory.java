package com.izettle.jdbi;

import java.sql.Array;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

public class PostgresArrayArgumentFactory implements ArgumentFactory<SqlArray<?>> {

    private final Class<?> clazz;

    public PostgresArrayArgumentFactory(Class<?> clazz) {
        this.clazz = clazz;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean accepts(final Class<?> expectedType, final Object value, final StatementContext ctx) {
        return value instanceof SqlArray && clazz.isAssignableFrom(((SqlArray) value).getType());
    }

    public Argument build(final Class<?> expectedType, final SqlArray<?> value, final StatementContext ctx) {
        return (position, statement, ctx1) -> {
            final Array elements = ctx.getConnection().createArrayOf(getTypeName(value), value.getElements());
            statement.setArray(position, elements);
        };
    }

    protected String getTypeName(final SqlArray<?> value) {

        Object[] elements = value.getElements();
        if (elements != null && elements.length > 0) {
            if (elements[0] instanceof Integer) {
                return "integer";
            } else if (elements[0] instanceof Long) {
                return "bigint";
            } else if (elements[0] instanceof Float) {
                return "float4";
            } else if (elements[0] instanceof Double) {
                return "float8";
            }
        }

        return "text";
    }
}