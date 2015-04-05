package com.izettle.jdbi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SqlArray<T> {

    private final Object[] elements;
    private final Class<T> type;

    public SqlArray(Class<T> type, Collection<T> elements) {
        this.elements = elements.toArray();
        this.type = type;
    }

    public static <T> SqlArray<T> arrayOf(Class<T> type, Iterable<T> elements) {
        return new SqlArray<>(type, getListFromIterable(elements));
    }

    private static <T> List<T> getListFromIterable(Iterable<T> elements) {
        List<T> listFromIterable = new ArrayList<>();

        elements.forEach(listFromIterable::add);

        return listFromIterable;
    }

    public Object[] getElements() {
        return elements.clone();
    }

    public Class<T> getType() {
        return type;
    }
}
