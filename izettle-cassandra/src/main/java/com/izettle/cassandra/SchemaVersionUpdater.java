package com.izettle.cassandra;

import static com.izettle.java.ResourceUtils.getBytesFromStream;
import static com.izettle.java.ValueChecks.coalesce;

import com.izettle.java.ResourceUtils;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ConsistencyLevel;
import com.netflix.astyanax.model.Row;
import com.netflix.astyanax.serializers.StringSerializer;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
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
 *         Keyspace myKeyspace = ..
 *         SchemaVersionUpdater updater = new SchemaVersionUpdater(myKeyspace);
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

    private final Keyspace keyspace;
    private final ColumnFamily<String, String> columnFamily;

    public SchemaVersionUpdater(Keyspace keyspace) {
        this.keyspace = keyspace;
        this.columnFamily = new ColumnFamily<>(COLUMN_FAMILY_NAME, StringSerializer.get(), StringSerializer.get());
    }

    public void applyFromResources(Class<?> clazz, String path) throws ConnectionException, IOException, URISyntaxException {
        List<SchemaUpdatingScript> scripts = new ArrayList<>();
        String resourcePath = path;
        if (!resourcePath.endsWith(File.separator)) {
            resourcePath += File.separator;
        }
        for (String resourceName : ResourceUtils.getResourceListing(clazz, path)) {
            URL url = clazz.getClassLoader().getResource(resourcePath + resourceName);
            // Take the first digits from the filename and use as "sequenceNr".
            int sequenceNr;
            try {
                sequenceNr = Integer.parseInt(resourceName.split("[^0-9]")[0]);
            } catch (NumberFormatException ignored) {
                throw new NumberFormatException("Cannot parse sequence number from resource filename \"" + resourceName + "\". Expected sequence number to be numeric first part of filename.");
            }
            scripts.add(new SchemaUpdatingScript(sequenceNr, resourceName, url));
        }
        apply(scripts);
    }

    private void apply(List<SchemaUpdatingScript> scripts) throws ConnectionException, IOException {
        LOG.debug("Updating Cassandra schema");
        ensureColumnFamilyExists();

        Set<String> alreadyExecutedScripts = new HashSet<>();
        for (Row<String, String> row : keyspace.prepareQuery(columnFamily).getAllRows().execute().getResult()) {
            alreadyExecutedScripts.add(row.getKey());
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
        Collections.sort(scripts, new Comparator<SchemaUpdatingScript>() {
            @Override
            public int compare(SchemaUpdatingScript a, SchemaUpdatingScript b) {
                return a.sequenceNr - b.sequenceNr;
            }
        });

        for (SchemaUpdatingScript script : scripts) {
            apply(script);
        }
    }

    private void ensureColumnFamilyExists() throws ConnectionException {
        if (keyspace.describeKeyspace().getColumnFamily(COLUMN_FAMILY_NAME) != null) {
            LOG.debug("Versioning column family already exists, skipping creation.");
            return;
        }

        LOG.info("Creating versioning column family.");
        keyspace.createColumnFamily(columnFamily, new HashMap<String, Object>());
        LOG.debug("Versioning column family created.");
    }

    private void apply(SchemaUpdatingScript script) throws ConnectionException, IOException {
        if (isAlreadyApplied(script)) {
            LOG.debug("Script " + script + " has already been applied (2nd check), skipping.");
            return;
        }

        LOG.info("Applying script " + script);
        keyspace.prepareCqlStatement()
                .withCql(script.readCQLContents())
                .execute();

        MutationBatch mutation = keyspace.prepareMutationBatch().withConsistencyLevel(ConsistencyLevel.CL_ALL);
        mutation.withRow(columnFamily, script.name).putColumn("executed", new Date());
        mutation.execute();
        LOG.debug("Script " + script + " successfully applied.");
    }

    private boolean isAlreadyApplied(SchemaUpdatingScript script) throws ConnectionException {
        return coalesce(keyspace.prepareQuery(columnFamily).getKey(script.name).getCount().execute().getResult(), 0) > 0;
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
                return new String(getBytesFromStream(is));
            }
        }

        @Override
        public String toString() {
            return "#" + sequenceNr + ": \"" + name + "\"";
        }
    }
}
