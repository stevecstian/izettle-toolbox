package izettle.cassandra;

import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.CassandraMapper;
import com.datastax.driver.mapping.MappingManagerModified;
import java.util.NoSuchElementException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class CassandraMapperTest {

    public static final User ALICE = new User(1, "alice@example.org", User.Gender.FEMALE);
    public static final User BOB = new User(2, "bob@example.org", User.Gender.MALE);

    private static Session session;

    @BeforeClass
    public static void setup() throws Exception {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra();
        Cluster cluster = Cluster
            .builder()
            .addContactPoint("127.0.0.1")
            .withPort(9142)
            .build();
        session = cluster.connect();
    }

    @Before
    public void before() {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
        session.execute("CREATE KEYSPACE test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };");
        session.execute("USE test;");
        session.execute("CREATE TABLE user (id int PRIMARY KEY, email varchar, gender varchar);");
    }

    @Test
    public void shouldSaveAndGetObject() throws Exception {

        MappingManagerModified mappingManager = new MappingManagerModified(session);

        CassandraMapper<User> mapper = new CassandraMapper<>(mappingManager, new UserMapper(), User.class);

        mapper.save(ALICE);

        assertThat(mapper.get(ALICE.getId()))
            .isEqualTo(ALICE);
    }

    @Test
    public void shouldQuerySingleObjects() throws Exception {

        MappingManagerModified mappingManager = new MappingManagerModified(session);

        CassandraMapper<User> mapper = new CassandraMapper<>(mappingManager, new UserMapper(), User.class);

        mapper.save(ALICE);

        assertThat(mapper.query("SELECT id, email, gender FROM user WHERE id = ?", ALICE.getId()))
            .isEqualTo(ALICE);

    }

    @Test
    public void shouldQueryMultipleObjects() throws Exception {

        MappingManagerModified mappingManager = new MappingManagerModified(session);

        CassandraMapper<User> mapper = new CassandraMapper<>(mappingManager, new UserMapper(), User.class);

        mapper.save(ALICE);
        mapper.save(BOB);

        assertThat(mapper.queryList("SELECT * FROM user WHERE id IN (1, 2);"))
            .containsExactly(ALICE, BOB);

    }

    @Test(expected = NoSuchElementException.class)
    public void shouldDeleteObject() throws Exception {

        MappingManagerModified mappingManager = new MappingManagerModified(session);

        CassandraMapper<User> mapper = new CassandraMapper<>(mappingManager, new UserMapper(), User.class);

        mapper.save(ALICE);
        mapper.delete(ALICE);

        mapper.get(ALICE.getId());
    }
}
