package com.izettle.cassandra;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.izettle.java.ResourceUtils.getBytesFromStream;

import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.izettle.java.ResourceUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class to run and keep track of CQL scripts for keeping a Cassandra keyspace up to date.
 *
 * Calling program can supply a directory containing CQL upgrade scripts that should be run when
 * starting up, and this class will create a column family to keep track of which scripts that
 * have been run, and execute the ones that need to be executed.
 *
 * Example: Let's say we have some scripts in a resource directory called "update-scripts" in our
 * program. Then we can make sure that all of those scripts have been executed when starting our
 * program by calling this:
 * <code>
 *     public static void main(String[] args) {
 *         Session session = cluster.connect(keySpace);
 *         SchemaVersionUpdater updater = new SchemaVersionUpdater(session);
 *         updater.applyFromResources(MyProgram.class, "update-scripts");
 *     }
 * </code>
 *
 * This class will create a column family called 'schema_scripts_version', that will contain
 * a row per script filename with a column 'executed' containing the date of when that
 * script was run.
 *
 * The name of the script files MUST start with a number, which is the sequence number.
 * Scripts will be executed in ascending sequence number order.
 */
public class SchemaVersionUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaVersionUpdater.class);
    private static final String COLUMN_FAMILY_NAME = "schema_scripts_version";

    private final Session session;

    public SchemaVersionUpdater(Session session) {
        this.session = session;
    }

    public void applyFromResources(Class<?> clazz, String path) throws IOException, URISyntaxException {
        List<SchemaUpdatingScript> scripts = new ArrayList<>();
        String resourcePath = path;
        for (String resourceName : ResourceUtils.getResourceListing(clazz, path)) {
            URL url = clazz.getClassLoader().getResource(resourcePath + resourceName);
            // Take the first digits from the filename and use as "sequenceNr".
            int sequenceNr = Integer.parseInt(resourceName.split("[^0-9]")[0]);
            scripts.add(new SchemaUpdatingScript(sequenceNr, resourceName, url));
        }
        apply(scripts);
    }

    private void apply(List<SchemaUpdatingScript> scripts) throws IOException {
        LOG.debug("Updating Cassandra schema");
        ensureColumnFamilyExists();

        Set<String> alreadyExecutedScripts = new HashSet<>();
        Statement select = QueryBuilder.select()
            .all()
            .from(COLUMN_FAMILY_NAME);
        ResultSet resultSet = session.execute(select);

        for (Row row : resultSet) {
            alreadyExecutedScripts.add(row.getString("key"));
        }

        Iterator<SchemaUpdatingScript> iterator = scripts.iterator();
        while (iterator.hasNext()) {
            SchemaUpdatingScript script = iterator.next();
            if (alreadyExecutedScripts.contains(script.name)) {
                LOG.debug("Script " + script.name + " has already been applied (1st check), skipping.");
                iterator.remove();
            }
        }

        // Sort in ascending sequence nr order
        Collections.sort(scripts, (a, b) -> a.sequenceNr - b.sequenceNr);

        for (SchemaUpdatingScript script : scripts) {
            apply(script);
        }
    }

    private void ensureColumnFamilyExists() {
        KeyspaceMetadata keyspaceMetadata =
            session.getCluster().getMetadata().getKeyspace(session.getLoggedKeyspace());

        if (keyspaceMetadata.getTable(COLUMN_FAMILY_NAME) != null) {
            LOG.debug("Versioning column family already exists, skipping creation.");
            return;
        }

        LOG.info("Creating versioning column family.");
        session.execute(
            "CREATE TABLE " + COLUMN_FAMILY_NAME + " ("
                + "key text PRIMARY KEY,"
                + "executed timestamp,"
                + ");");

        LOG.debug("Versioning column family created.");
    }

    private void apply(SchemaUpdatingScript script) throws IOException {
        if (isNotApplied(script)) {

            LOG.info("Applying script " + script);
            session.execute(script.readCQLContents());

            Insert insert = QueryBuilder.insertInto(COLUMN_FAMILY_NAME)
                .value("key", script.name)
                .value("executed", new Date());
            session.execute(insert);

            LOG.debug("Script " + script + " successfully applied.");
        }
    }

    private boolean isNotApplied(SchemaUpdatingScript script) {
        Statement select = QueryBuilder.select()
            .all()
            .from(COLUMN_FAMILY_NAME)
            .where(eq("key", script.name));
        return session.execute(select).isExhausted();
    }

    private static final class SchemaUpdatingScript {
        private final int sequenceNr;
        private final String name;
        private final URL url;

        private SchemaUpdatingScript(int sequenceNr, String name, URL url) {
            this.sequenceNr = sequenceNr;
            this.name = name;
            this.url = url;
        }

        public String readCQLContents() throws IOException {
            try (InputStream is = url.openStream()) {
                return new String(getBytesFromStream(is), "UTF-8");
            }
        }

        @Override
        public String toString() {
            return "#" + sequenceNr + ": \"" + name + "\"";
        }
    }
}
