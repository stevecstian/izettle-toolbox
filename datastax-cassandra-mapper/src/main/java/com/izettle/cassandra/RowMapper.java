package com.izettle.cassandra;

@FunctionalInterface
public interface RowMapper<T> {

    T map(DataRow values);

}
