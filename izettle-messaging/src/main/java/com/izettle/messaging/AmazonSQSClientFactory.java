package com.izettle.messaging;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that creates Amazon SQS clients for specific endpoints. This factory should be used to ensure that only
 * one client is created per endpoint.
 */
public class AmazonSQSClientFactory {
	private static final Logger LOG = LoggerFactory.getLogger(AmazonSQSClientFactory.class);
	private static final Map<String, AmazonSQSAsync> clients = new ConcurrentHashMap<>();

	/**
	 * Creates Amazon SQS client for given endpoint using the provided credentials.
	 *
	 * @param endpoint Amazon SQS endpoint.
	 * @param awsCredentials AWS credentials with access to the endpoint.
	 * @return Amazon SQS client.
	 */
	public static AmazonSQSAsync getInstance(String endpoint, AWSCredentials awsCredentials) {

		LOG.info(String.format("Creating AWS client for endpoint %s", endpoint));

		if (!clients.containsKey(endpoint)) {
			AmazonSQSAsync amazonSQSClient = new AmazonSQSAsyncClient(awsCredentials);
			amazonSQSClient.setEndpoint(endpoint);
			clients.put(endpoint, amazonSQSClient);
		}

		return clients.get(endpoint);
	}
}
