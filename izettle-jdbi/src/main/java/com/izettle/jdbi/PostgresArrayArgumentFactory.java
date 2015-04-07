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
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof SqlArray && clazz.isAssignableFrom(((SqlArray) value).getType());
    }

    public Argument build(Class<?> expectedType, final SqlArray<?> value, StatementContext ctx) {
        return (position, statement, ctx1) -> {
            final Array ary = ctx.getConnection().createArrayOf(getTypeName(value), value.getElements());
            statement.setArray(position, ary);
        };
    }

    private String getTypeName(final SqlArray<?> value) {

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
            } else if (elements[0] instanceof Boolean) {
                return "bool";
            }
        }

        return "text";
    }
}