package com.izettle.cassandra;

import static com.google.common.truth.Truth.assertThat;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.DataLoader;
import org.cassandraunit.dataset.yaml.ClassPathYamlDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SchemaVersionUpdaterTest {
    final private static String TABLE_NAME = "schema_migration";

    @BeforeClass
    public static void beforeClass() throws InterruptedException, IOException, TTransportException {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
    }

    @Before
    public void before() {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    @Test
    public void updateFromLegacyColumnFamily() throws IOException, URISyntaxException {
        load("dataset-legacy.yaml");

        Session session = getSession();
        SchemaVersionUpdater updater = new SchemaVersionUpdater(session);
        updater.applyFromResources(SchemaVersionUpdaterTest.class, "migrations/");

        ResultSet rs = session.execute("SELECT * FROM " + TABLE_NAME);
        List<Row> rows = rs.all();
        assertThat(rows.size()).isEqualTo(5);
        rows.forEach(row -> {
            switch (row.getString("key")) {
                case "0000-script1.cql":
                    assertThat(row.getTimestamp("executed").getTime())
                        .isEqualTo(Instant.parse("2016-09-01T00:00:00Z").toEpochMilli());
                    break;
                case "0001-script2.cql":
                    assertThat(row.getTimestamp("executed").getTime())
                        .isEqualTo(Instant.parse("2016-09-02T00:00:00Z").toEpochMilli());
                    break;
                case "0002-script3.cql":
                    assertThat(row.getTimestamp("executed").getTime())
                        .isEqualTo(Instant.parse("2016-09-03T00:00:00Z").toEpochMilli());
                    break;
            }
        });
    }

    @Test
    public void doNotUpdateFromLegacyColumnFamily() throws IOException, URISyntaxException {
        load("dataset-legacy.yaml");

        Session session = getSession();
        createSchemaMigrationTable(session);
        session.execute(QueryBuilder.insertInto(TABLE_NAME)
            .value("key", "0123-foo.cql")
            .value("executed", new Date())
        );

        SchemaVersionUpdater updater = new SchemaVersionUpdater(session);
        updater.applyFromResources(SchemaVersionUpdaterTest.class, "migrations/");

        ResultSet rs = session.execute("SELECT * FROM " + TABLE_NAME);
        List<Row> rows = rs.all();
        assertThat(rows.size()).isEqualTo(3);
        List<String> keys = rows.stream().map(row -> row.getString("key")).collect(Collectors.toList());
        assertThat(keys).containsAllOf(
            "0123-foo.cql",
            "0003-before-the-big-bang.cql",
            "0004-the-big-bang.cql"
        );
    }

    @Test
    public void doNotApplyScriptAlreadyApplied() throws IOException, URISyntaxException {
        load("dataset-empty.yaml");

        Session session = getSession();
        createSchemaMigrationTable(session);
        session.execute(QueryBuilder.insertInto(TABLE_NAME)
            .value("key", "0003-before-the-big-bang.cql")
            .value("executed", new Date())
        );

        SchemaVersionUpdater updater = new SchemaVersionUpdater(session);
        updater.applyFromResources(SchemaVersionUpdaterTest.class, "migrations/");

        KeyspaceMetadata keyspaceMetadata =
            session.getCluster().getMetadata().getKeyspace(session.getLoggedKeyspace());
        assertThat(keyspaceMetadata.getTable("galaxies")).isNull();
    }

    private void load(String dataSetLocation) {
        DataLoader dataLoader = new DataLoader("TestCluster", "localhost:9171");
        dataLoader.load(new ClassPathYamlDataSet(dataSetLocation));
    }

    private static Session getSession() {
        return Cluster.builder()
            .addContactPoint("127.0.0.1").withPort(9142)
            .build()
            .connect("schema_migration_test");
    }

    private static void createSchemaMigrationTable(Session session) {
        session.execute("CREATE TABLE " + TABLE_NAME + " ("
            + "key text PRIMARY KEY,"
            + "executed timestamp"
            + ");");
    }
}
