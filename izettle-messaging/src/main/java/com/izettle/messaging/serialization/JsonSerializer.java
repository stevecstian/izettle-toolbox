package com.izettle.messaging.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonSerializer {
	private final static ObjectMapper jsonMapper = createInstance();

	private static ObjectMapper createInstance() {
		ObjectMapper result = new ObjectMapper();
		result.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		return result;
	}

	public static ObjectMapper getInstance() {
		return jsonMapper;
	}
}
