package com.izettle.jdbi;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.Binder;

@SuppressWarnings("rawtypes")
public class EnumNameArrayBinder implements Binder<Bind, Collection<Enum>> {

    @Override
    public void bind(SQLStatement<?> q, Bind bind, Collection<Enum> arg) {
        Set<String> names =
            arg
                .stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        q.bind(bind.value(), SqlArray.arrayOf(String.class, names));
    }
}