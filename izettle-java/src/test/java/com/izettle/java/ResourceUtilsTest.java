package com.izettle.java;

import static com.izettle.java.ResourceUtils.getResourceAsBytes;
import static com.izettle.java.ResourceUtils.getResourceAsStream;
import static com.izettle.java.ResourceUtils.getResourceListing;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import org.junit.Test;

public class ResourceUtilsTest {
    @Test
    public void testGetResourceBytes() throws Exception {
        byte[] bytes1 = getResourceAsBytes("resourceloadertestfile.txt");
        assertThat(new String(bytes1))
            .isNotEmpty()
            .isEqualTo("test file for resource loading");

        byte[] bytes2 = getResourceAsBytes("/resourceloadertestfile.txt");
        assertThat(new String(bytes2))
            .isNotEmpty()
            .isEqualTo("test file for resource loading");
    }

    @Test
    public void testGetResourceAsStream() throws Exception {
        try (InputStream inputStream = getResourceAsStream("resourceloadertestfile.txt")) {
            assertThat(inputStream).isNotNull();
        }

        try (InputStream inputStream = getResourceAsStream("/resourceloadertestfile.txt")) {
            assertThat(inputStream).isNotNull();
        }
    }

    @Test
    public void testGetResourceAsBytesWithContextClass() throws Exception {
        byte[] resource = getResourceAsBytes(ResourceUtilsTest.class, "resourcewithcontexttestfile.txt");
        assertThat(new String(resource))
            .isNotEmpty()
            .isEqualTo("test file for resource loading given a context class");
    }

    @Test
    public void testGetResourceAsStreamWithContextClass() throws Exception {
        try (InputStream stream = getResourceAsStream(ResourceUtilsTest.class, "resourcewithcontexttestfile.txt")) {
            assertThat(stream).isNotNull();
        }
    }

    @Test
    public void testGetResourceListingShouldReturnAllFilesInResourceDirectory() throws Exception {
        String[] files = getResourceListing(ResourceUtilsTest.class, "resourceloadertest");
        assertThat(files).isNotEmpty();
        assertThat(files).contains("file1.txt");
        assertThat(files).contains("file2.txt");
        assertThat(files).doesNotContain("resourceloadertestfile.txt");
    }
}
