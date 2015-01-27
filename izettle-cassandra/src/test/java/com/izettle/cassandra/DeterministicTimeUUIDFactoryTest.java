package com.izettle.cassandra;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;
import java.util.UUID;
import org.junit.Test;

public class DeterministicTimeUUIDFactoryTest {

    @Test
    public void test() {
        Date date = new Date();
        UUID uuid = DeterministicTimeUUIDFactory.create(UUID.randomUUID(), date);

        // The UUID should be of version 1 (Time UUID)
        assertThat(uuid.version()).isEqualTo(1);

        // The UUID timestamp should be same as original timestamp
        assertThat(uuid.timestamp()).isEqualTo(date.getTime());
    }
}
