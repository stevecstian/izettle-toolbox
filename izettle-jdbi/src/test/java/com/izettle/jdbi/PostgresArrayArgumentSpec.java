package com.izettle.jdbi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.skife.jdbi.v2.StatementContext;

public class PostgresArrayArgumentSpec {
    private static final PostgresArrayArgumentFactory LONG_FACTORY = new PostgresArrayArgumentFactory(Long.class);

    @Test
    public void testAccept() {
        List<Long> longs = new ArrayList<>();
        longs.add(1L);

        List<String> strings = new ArrayList<>();
        strings.add("string");

        assertTrue(LONG_FACTORY.accepts(Long.class, new SqlArray<>(Long.class, longs), mock(StatementContext.class)));
        assertFalse(LONG_FACTORY.accepts(Long.class, "shouldFail", mock(StatementContext.class)));
        assertFalse(LONG_FACTORY.accepts(Long.class, new SqlArray<>(String.class, strings), mock(StatementContext.class)));
    }

    @Test
    public void testGetTypeName() {
        assertThat(LONG_FACTORY.getTypeName(new SqlArray<>(Long.class, toList(1L)))).isEqualTo("bigint");
        assertThat(LONG_FACTORY.getTypeName(new SqlArray<>(Integer.class, toList(1)))).isEqualTo("integer");
        assertThat(LONG_FACTORY.getTypeName(new SqlArray<>(Double.class, toList(1.1)))).isEqualTo("float8");
        assertThat(LONG_FACTORY.getTypeName(new SqlArray<>(Float.class, toList(1.1F)))).isEqualTo("float4");
        assertThat(LONG_FACTORY.getTypeName(new SqlArray<>(String.class, toList("test")))).isEqualTo("text");
    }

    private <T> List<T> toList(T value) {
        return Arrays.asList(value);
    }
}
