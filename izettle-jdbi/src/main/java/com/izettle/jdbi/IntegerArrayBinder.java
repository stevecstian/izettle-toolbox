package com.izettle.jdbi;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.Binder;

public class IntegerArrayBinder implements Binder<Bind, Iterable<Long>> {

    @Override
    public void bind(SQLStatement<?> q, Bind bind, Iterable<Long> arg) {
        q.bind(bind.value(), SqlArray.arrayOf(Long.class, arg));
    }
}
