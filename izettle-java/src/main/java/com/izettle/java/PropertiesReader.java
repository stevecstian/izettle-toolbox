package com.izettle.java;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {

	private PropertiesReader() {
	}

	public static Properties loadProperties(String fileName) {
		Properties pro = new Properties();
		InputStream in = null;
		try {
			in = ResourceUtils.getResourceAsStream(fileName);
			if (in == null) {
				throw new RuntimeException(" File not found, name: " + fileName);
			}
			pro.load(in);
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage() + " File name: " + fileName, e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				//Do nothing
			}
		}
		return pro;
	}
}
