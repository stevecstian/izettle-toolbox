package com.izettle.jdbi;

import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.Binder;

public class EnumOrdinalBinder implements Binder<Bind, Enum> {

    @Override
    public void bind(final SQLStatement<?> q, final Bind bind, final Enum arg) {
        q.bind(bind.value(), arg.ordinal());
    }
}
