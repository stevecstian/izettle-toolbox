package com.izettle.jdbi;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.Binder;

public class StringArrayBinder implements Binder<Bind, Iterable<String>> {

    @Override
    public void bind(SQLStatement<?> q, Bind bind, Iterable<String> arg) {
        q.bind(bind.value(), SqlArray.arrayOf(String.class, arg));
    }
}
