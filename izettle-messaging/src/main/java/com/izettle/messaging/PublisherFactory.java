package com.izettle.messaging;

import com.izettle.java.PropertiesReader;
import java.util.Properties;

public class PublisherFactory {

	private static final Properties properties = PropertiesReader.loadProperties("messaging.properties");

	private static <T> MessageQueueProducer<T> createPublisher(String topicPropertyName) throws MessagingException {
		return PublisherService.nonEncryptedPublisherService(
				properties.getProperty(topicPropertyName),
				properties.getProperty("accessKey"),
				properties.getProperty("secretKey"),
				properties.getProperty("awsSnsEndpoint"));
	}

}
