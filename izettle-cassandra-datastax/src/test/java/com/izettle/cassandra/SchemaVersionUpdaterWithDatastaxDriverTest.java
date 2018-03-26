package com.izettle.cassandra;

import static com.google.common.truth.Truth.assertThat;
import static com.izettle.cassandra.SchemaVersionUpdaterWithDatastaxDriver.TABLE_NAME;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Set;
import me.prettyprint.cassandra.service.CassandraHost;
import org.cassandraunit.CassandraUnit;
import org.cassandraunit.DataLoader;
import org.cassandraunit.dataset.yaml.ClassPathYamlDataSet;
import org.junit.Rule;
import org.junit.Test;

public class SchemaVersionUpdaterWithDatastaxDriverTest {

    @Rule
    public CassandraUnit cassandraUnit = new CassandraUnit(new ClassPathYamlDataSet("dataset-empty.yaml"));

    @Test(expected = InvalidQueryException.class)
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

        KeyspaceMetadata keyspaceMetadata = session.getCluster().getMetadata()
            .getKeyspace(session.getLoggedKeyspace());

        assertThat(keyspaceMetadata.getTable("galaxies")).isNull();
        assertThat(keyspaceMetadata.getTable("planets")).isNotNull();
    }

    @Test
    public void applyScriptsIfNotYetDone() throws IOException, URISyntaxException {
        load("dataset-empty.yaml");

        Session session = getSession();

        SchemaVersionUpdaterWithDatastaxDriver updater = new SchemaVersionUpdaterWithDatastaxDriver(session);
        updater.applyFromResources(SchemaVersionUpdaterWithDatastaxDriverTest.class, "migrations");

        KeyspaceMetadata keyspaceMetadata = session.getCluster().getMetadata()
            .getKeyspace(session.getLoggedKeyspace());

        assertThat(keyspaceMetadata.getTable("galaxies")).isNotNull();
        assertThat(keyspaceMetadata.getTable("planets")).isNotNull();
    }

    private void load(String dataSetLocation) {
        CassandraHost host = getCassandraHost();
        DataLoader dataLoader = new DataLoader("TestCluster", host.getUrl());
        dataLoader.load(new ClassPathYamlDataSet(dataSetLocation));
    }

    private CassandraHost getCassandraHost() {
        Set<CassandraHost> hosts = cassandraUnit.cluster.getConnectionManager().getHosts();
        if (hosts.isEmpty()) {
            throw new IllegalStateException("Cannot run test, no Cassandra hosts available");
        }
        return hosts.iterator().next();
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
