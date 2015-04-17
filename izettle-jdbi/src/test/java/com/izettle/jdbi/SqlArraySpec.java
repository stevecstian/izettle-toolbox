package com.izettle.jdbi;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class SqlArraySpec {
    @Test
    public void testArrayOf() {
        List<String> strings = new ArrayList<>();
        strings.add("test1");
        strings.add("test2");

        assertThat(SqlArray.arrayOf(String.class, strings).getElements()).isEqualTo(strings.toArray());
        assertThat(SqlArray.arrayOf(String.class, new ArrayList<>()).getElements()).isEmpty();
    }
}
