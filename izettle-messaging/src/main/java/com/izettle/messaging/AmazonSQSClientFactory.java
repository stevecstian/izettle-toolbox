package com.izettle.messaging;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that creates Amazon SQS clients for specific endpoints. This factory should be used to ensure that only
 * one client is created per endpoint.
 */
public class AmazonSQSClientFactory {
	private static final Logger LOG = LoggerFactory.getLogger(AmazonSQSClientFactory.class);
	private static final Map<String, AmazonSQSClient> clients = new HashMap<>();

	/**
	 * Creates Amazon SQS client for given endpoint using the provided credentials.
	 *
	 * @param endpoint Amazon SQS endpoint.
	 * @param awsCredentials AWS credentials with access to the endpoint.
	 * @return Amazon SQS client.
	 */
	public static synchronized AmazonSQSClient getInstance(String endpoint, AWSCredentials awsCredentials) {

		LOG.info(String.format("Creating AWS client for endpoint %s", endpoint));

		if (!clients.containsKey(endpoint)) {
			AmazonSQSClient amazonSQSClient = new AmazonSQSClient(awsCredentials);
			amazonSQSClient.setEndpoint(endpoint);
			clients.put(endpoint, amazonSQSClient);
		}

		return clients.get(endpoint);
	}
}
