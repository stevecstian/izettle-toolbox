package com.izettle.jdbi;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;

@RunWith(MockitoJUnitRunner.class)
public class PostgresArrayArgumentFactoryTest {
    PostgresArrayArgumentFactory sut;
    @Mock
    StatementContext ctx;
    @Mock
    PreparedStatement preparedStatement;
    @Mock
    Connection connection;
    @Mock
    Array array;

    @Before
    public void setUp() throws Exception {
        sut = new PostgresArrayArgumentFactory(Integer.class);
        given(ctx.getConnection()).willReturn(connection);
    }

    @Test
    public void testBuildInteger() throws Exception {
        Integer[] intArray = {1, 2, 3};
        List<Integer> ints = Arrays.asList(intArray);
        SqlArray<Integer> value = new SqlArray<>(Integer.class, ints);
        given(connection.createArrayOf("integer", intArray)).willReturn(array);
        Argument result = sut.build(Integer.class, value, ctx);
        result.apply(1, preparedStatement, ctx);
        verify(preparedStatement).setArray(1, array);
    }

    @Test
    public void testBuildLong() throws Exception {
        Long[] longArray = {1L, 2L, 3L};
        List<Long> longs = Arrays.asList(longArray);
        SqlArray<Long> value = new SqlArray<>(Long.class, longs);
        given(connection.createArrayOf("bigint", longArray)).willReturn(array);
        Argument result = sut.build(Long.class, value, ctx);
        result.apply(1, preparedStatement, ctx);
        verify(preparedStatement).setArray(1, array);
    }

    @Test
    public void testBuildFloat() throws Exception {
        Float[] floatArray = {1.1F, 2.2F, 3.3F};
        List<Float> floats = Arrays.asList(floatArray);
        SqlArray<Float> value = new SqlArray<>(Float.class, floats);
        given(connection.createArrayOf("float4", floatArray)).willReturn(array);
        Argument result = sut.build(Float.class, value, ctx);
        result.apply(1, preparedStatement, ctx);
        verify(preparedStatement).setArray(1, array);
    }

    @Test
    public void testBuildDouble() throws Exception {
        Double[] doubleArray = {1.1D, 2.2D, 3.3D};
        List<Double> doubles = Arrays.asList(doubleArray);
        SqlArray<Double> value = new SqlArray<>(Double.class, doubles);
        given(connection.createArrayOf("float8", doubleArray)).willReturn(array);
        Argument result = sut.build(Float.class, value, ctx);
        result.apply(1, preparedStatement, ctx);
        verify(preparedStatement).setArray(1, array);
    }

    @Test
    public void testBuildString() throws Exception {
        String[] stringArray = {"aa", "bb", "cc"};
        List<String> strings = Arrays.asList(stringArray);
        SqlArray<String> value = new SqlArray<>(String.class, strings);
        given(connection.createArrayOf("text", stringArray)).willReturn(array);
        Argument result = sut.build(Float.class, value, ctx);
        result.apply(1, preparedStatement, ctx);
        verify(preparedStatement).setArray(1, array);
    }

    @Test
    public void testBuildZero() throws Exception {
        Integer[] intArray = {1, 2, 3};
        List<Integer> ints = Arrays.asList(intArray);
        SqlArray<Integer> value = new SqlArray<>(Integer.class, ints);
        given(connection.createArrayOf("integer", intArray)).willReturn(null);
        Argument result = sut.build(Integer.class, value, ctx);
        result.apply(1, preparedStatement, ctx);
        verify(preparedStatement).setArray(1, null);
    }
}
