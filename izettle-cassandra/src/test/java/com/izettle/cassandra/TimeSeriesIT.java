package com.izettle.cassandra;


import static org.assertj.core.api.Assertions.assertThat;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolType;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.serializers.StringSerializer;
import com.netflix.astyanax.serializers.UUIDSerializer;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.cassandraunit.AbstractCassandraUnit4TestCase;
import org.cassandraunit.dataset.DataSet;
import org.cassandraunit.dataset.yaml.ClassPathYamlDataSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TimeSeriesIT extends AbstractCassandraUnit4TestCase {

    private static Keyspace keyspace;
    private static AstyanaxContext<Keyspace> astyanaxContext;

    @BeforeClass
    public static void beforeClass() {

        astyanaxContext =
                new AstyanaxContext.Builder()
                        .forKeyspace("timeSeriesKeyspace")
                        .withConnectionPoolConfiguration(
                                new ConnectionPoolConfigurationImpl("myCPConfig")
                                        .setSeeds("localhost")
                                        .setPort(9171))
                        .withAstyanaxConfiguration(
                                new AstyanaxConfigurationImpl()
                                        .setConnectionPoolType(ConnectionPoolType.TOKEN_AWARE)
                                        .setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE))
                        .buildKeyspace(ThriftFamilyFactory.getInstance());

        keyspace = astyanaxContext.getClient();
        astyanaxContext.start();
    }

    @AfterClass
    public static void afterClass() {
        astyanaxContext.shutdown();
    }

    @Test
    public void addEventsByUUIDAndRetrieveTimePeriod() throws IOException, ConnectionException {

        TimeSeries<String> timeSeries = new TimeSeries<>(keyspace, new ColumnFamily<>("timeSeriesColumnFamily", StringSerializer.get(), UUIDSerializer.get()));

        String key = "key";

        timeSeries.add(key, UUID.randomUUID(), new Date(10), "Data1");
        timeSeries.add(key, UUID.randomUUID(), new Date(20), "Data21");
        timeSeries.add(key, UUID.randomUUID(), new Date(20), "Data22");
        timeSeries.add(key, UUID.randomUUID(), new Date(30), "Data3");
        timeSeries.add(key, UUID.randomUUID(), new Date(40), "Data4");

        List<String> result = timeSeries.get(key, new Date(20), new Date(31), false, 100);

        assertThat(result.size()).isEqualTo(3);
    }

    @Test
    public void addEventsByStringAndRetrieveTimePeriod() throws IOException, ConnectionException {

        TimeSeries<String> timeSeries = new TimeSeries<>(keyspace, new ColumnFamily<>("timeSeriesColumnFamily", StringSerializer.get(), UUIDSerializer.get()));

        String key = "key";

        timeSeries.add(key, "Event 1", new Date(10), "Data1");
        timeSeries.add(key, "Event 2", new Date(20), "Data21");
        timeSeries.add(key, "Event 3", new Date(20), "Data22");
        timeSeries.add(key, "Event 3", new Date(20), "Data22 (duplicate will overwrite)");
        timeSeries.add(key, "Event 4", new Date(30), "Data3");
        timeSeries.add(key, "Event 5", new Date(40), "Data4");

        List<String> result = timeSeries.get(key, new Date(20), new Date(31), false, 100);

        assertThat(result.size()).isEqualTo(3);
    }

    @Override
    public DataSet getDataSet() {
        return new ClassPathYamlDataSet("cassandraDataSet.yaml");
    }
}
