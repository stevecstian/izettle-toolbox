package com.izettle.java;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class HttpRequestValidatingHelperTest {
    private String inputString;
    private Boolean expectedResult;
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Parameterized.Parameters
    public static Collection primeNumbers() {
        return Arrays.asList(new Object[][]{
            {"one\rtwo", false},
            {"one\ntwo", false},
            {"one\r\ntwo", false},
            {"one\r\ntwo\r\n", false},
            {"\r\none\r\ntwo\r\n", false},
            {"\r\none\r\ntwo\r\n", false},
            {"onetwo", true},
            {"one two", true},
        });
    }

    public HttpRequestValidatingHelperTest(
        String inputString,
        Boolean expectedResult
    ) {
        this.inputString = inputString;
        this.expectedResult = expectedResult;
    }

    @Test
    public void shouldGuardValueAgainstResponseSplitterAttack() throws Exception {
        if (!expectedResult) {
            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage("CR and/or LF found in param to be used in response header");
        }
        String result = HttpRequestValidatingHelper.checkValueForNewline(inputString);
        assertThat(result, is(inputString));
    }
}
