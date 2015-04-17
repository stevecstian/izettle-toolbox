package com.izettle.jdbi;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class SqlArray<T> {

    private final Object[] elements;
    private final Class<T> type;

    public SqlArray(final Class<T> type, final Collection<T> elements) {
        this.elements = elements.toArray();
        this.type = type;
    }

    public static <T> SqlArray<T> arrayOf(final Class<T> type, final Iterable<T> elements) {
        return new SqlArray<>(type, toList(elements));
    }

    private static <T> List<T> toList(final Iterable<T> elements) {
        return StreamSupport.stream(elements.spliterator(), false).collect(Collectors.toList());
    }

    public Object[] getElements() {
        return elements.clone();
    }

    public Class<T> getType() {
        return type;
    }
}
