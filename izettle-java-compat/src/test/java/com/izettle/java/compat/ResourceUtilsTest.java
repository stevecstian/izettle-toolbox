package com.izettle.java.compat;

import static com.izettle.java.compat.ResourceUtils.getResourceAsBytes;
import static com.izettle.java.compat.ResourceUtils.getResourceAsStream;
import static com.izettle.java.compat.ResourceUtils.getResourceListing;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import org.junit.Test;

public class ResourceUtilsTest {
    @Test
    public void testGetResourceBytes() throws Exception {
        byte[] bytes = getResourceAsBytes("resourceloadertestfile.txt");
        assertThat(new String(bytes))
                .isNotEmpty()
                .isEqualTo("test file for resource loading");

        bytes = getResourceAsBytes("/resourceloadertestfile.txt");
        assertThat(new String(bytes))
                .isNotEmpty()
                .isEqualTo("test file for resource loading");
    }

    @Test
    public void testGetResourceAsStream() throws Exception {
        InputStream inputStream = getResourceAsStream("resourceloadertestfile.txt");
        assertThat(inputStream).isNotNull();
        inputStream.close();

        inputStream = getResourceAsStream("/resourceloadertestfile.txt");
        assertThat(inputStream).isNotNull();
        inputStream.close();
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
        InputStream stream = getResourceAsStream(ResourceUtilsTest.class, "resourcewithcontexttestfile.txt");
        assertThat(stream).isNotNull();
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
