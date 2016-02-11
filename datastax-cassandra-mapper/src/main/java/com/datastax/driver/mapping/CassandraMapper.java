package com.datastax.driver.mapping;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.izettle.cassandra.MapBackedDataRow;
import com.izettle.cassandra.RowBackedDataRow;
import com.izettle.cassandra.RowMapper;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;

public class CassandraMapper<T> {

    private final Session session;
    private final Mapper<T> mapper;
    private final RowMapper<T> rowMapper;
    private final MappingManagerModified mappingManager;
    private Class<T> klass;

    public CassandraMapper(MappingManagerModified mappingManager, RowMapper<T> rowMapper, Class<T> klass) {
        this.mappingManager = requireNonNull(mappingManager);
        this.rowMapper = requireNonNull(rowMapper);
        this.klass = requireNonNull(klass);
        this.mapper = mappingManager.mapper(klass);
        this.session = mapper.getManager().getSession();
    }

    public T get(Object... objects) {
        ResultSet resultSet = session.execute(mapper.getQuery(objects));

        Row row = resultSet.one();
        if (row == null) {
            throw new NoSuchElementException();
        }

        return rowMapper.map(new MapBackedDataRow(toMap(row)));
    }

    public T query(String query, Object... values) {
        ResultSet resultSet = session.execute(query, values);

        Row row = resultSet.one();
        if (row == null) {
            throw new NoSuchElementException();
        }

        return rowMapper.map(new RowBackedDataRow(row));
    }

    public List<T> queryList(String query, Object... values) {
        ResultSet resultSet = session.execute(query, values);

        return StreamSupport.stream(resultSet.spliterator(), false)
            .map(RowBackedDataRow::new)
            .map(rowMapper::map)
            .collect(toList());
    }

    public void save(T entity, Mapper.Option... options) {
        mapper.save(entity, options);
    }

    public void delete(T entity) {
        mapper.delete(entity);
    }

    private Map<String, Object> toMap(Row row) {
        Map<String, Object> values = new HashMap<>();

        EntityMapper<T> entityMapper = mappingManager.entityMapper(klass);

        for (ColumnMapper<T> cm : entityMapper.allColumns()) {
            String name = cm.getAlias() != null ? cm.getAlias() : cm.getColumnName();
            if (!row.getColumnDefinitions().contains(name)) {
                continue;
            }
            ByteBuffer bytes = row.getBytesUnsafe(name);
            if (bytes != null) {
                values.put(
                    cm.getColumnName().replace("\"", ""),
                    cm.getDataType()
                        .deserialize(
                            bytes,
                            mappingManager.getSession()
                                .getCluster()
                                .getConfiguration()
                                .getProtocolOptions()
                                .getProtocolVersionEnum()
                        )
                );
            }
        }
        return values;
    }
}
