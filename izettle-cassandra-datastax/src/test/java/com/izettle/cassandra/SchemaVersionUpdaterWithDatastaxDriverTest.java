package com.izettle.cassandra;

import static com.google.common.truth.Truth.assertThat;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.DataLoader;
import org.cassandraunit.dataset.yaml.ClassPathYamlDataSet;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SchemaVersionUpdaterWithDatastaxDriverTest {
    final private static String TABLE_NAME = "schema_scripts_version";

    @BeforeClass
    public static void beforeClass() throws InterruptedException, IOException, TTransportException {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
    }

    @Before
    public void before() {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    @Test(expected = IllegalStateException.class)
    public void dieIfTableSchemaIsIncorrect() throws IOException, URISyntaxException {
        load("dataset-legacy.yaml");

        Session session = getSession();
        new SchemaVersionUpdaterWithDatastaxDriver(session)
            .applyFromResources(SchemaVersionUpdaterWithDatastaxDriverTest.class, "migrations");
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

        SchemaVersionUpdaterWithDatastaxDriver updater = new SchemaVersionUpdaterWithDatastaxDriver(session);
        updater.applyFromResources(SchemaVersionUpdaterWithDatastaxDriverTest.class, "migrations");

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
