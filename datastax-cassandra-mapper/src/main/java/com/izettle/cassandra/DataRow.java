package com.izettle.cassandra;

public interface DataRow {

    String getString(String name);

    int getInt(String name);
}
