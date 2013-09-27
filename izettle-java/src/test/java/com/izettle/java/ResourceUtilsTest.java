package com.izettle.java;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import org.junit.Test;

public class ResourceUtilsTest {
	@Test
	public void testGetResourceBytes() throws Exception {
		byte[] bytes = ResourceUtils.getResourceAsBytes("resourceloadertestfile.txt");
		assertTrue(bytes.length > 0);

		bytes = ResourceUtils.getResourceAsBytes("/resourceloadertestfile.txt");
		assertTrue(bytes.length > 0);
	}

	@Test
	public void testGetResourceAsStream() throws Exception {
		InputStream inputStream = ResourceUtils.getResourceAsStream("resourceloadertestfile.txt");
		assertNotNull(inputStream);
		inputStream.close();

		inputStream = ResourceUtils.getResourceAsStream("/resourceloadertestfile.txt");
		assertNotNull(inputStream);
		inputStream.close();
	}
}
