package com.izettle.messaging;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.izettle.java.PropertiesReader;
import java.util.Properties;

public class PublisherFactory {

	private static final Properties properties = PropertiesReader.loadProperties("messaging.properties");

	private static <T> MessageQueueProducer<T> createPublisher(String topicPropertyName) throws MessagingException {
		AWSCredentials credentials = AWSCredentialsWrapper.getCredentials(
				properties.getProperty("accessKey"),
				properties.getProperty("secretKey")
		);
		AmazonSNSClient client = new AmazonSNSClient(credentials);
		client.setEndpoint(properties.getProperty("awsSnsEndpoint"));
		return PublisherService.nonEncryptedPublisherService(
				client,
				properties.getProperty(topicPropertyName)
			);
	}
}
