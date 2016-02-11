package com.izettle.cassandra;

import com.datastax.driver.core.Row;

public class RowBackedDataRow implements DataRow {

    private final Row row;

    public RowBackedDataRow(Row row) {
        this.row = row;
    }

    @Override
    public String getString(String name) {
        return row.getString(name);
    }

    @Override
    public int getInt(String name) {
        return row.getInt(name);
    }
}
