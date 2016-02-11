package com.izettle.cassandra;

import java.util.Map;

public class MapBackedDataRow implements DataRow {

    private final Map<String, Object> map;

    public MapBackedDataRow(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public String getString(String name) {
        return (String) map.get(name);
    }

    @Override
    public int getInt(String name) {
        return (int) map.get(name);
    }
}
