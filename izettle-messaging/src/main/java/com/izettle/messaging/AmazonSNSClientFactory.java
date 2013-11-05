package com.izettle.messaging;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that creates Amazon SNS clients for specific endpoints. This factory should be used to ensure that only
 * one client is created per endpoint.
 */
public class AmazonSNSClientFactory {
	private static final Logger LOG = LoggerFactory.getLogger(AmazonSNSClientFactory.class);
	private static final Map<String, AmazonSNSAsync> clients = new ConcurrentHashMap<>();

	/**
	 * Creates Amazon SNS client for given endpoint using the provided credentials.
	 *
	 * @param endpoint Amazon SNS endpoint.
	 * @param awsCredentials AWS credentials with access to the endpoint.
	 * @return Amazon SNS client.
	 */
	public static AmazonSNSAsync getInstance(String endpoint, AWSCredentials awsCredentials) {
		if (!clients.containsKey(endpoint)) {
			LOG.info(String.format("Creating AWS client for endpoint %s", endpoint));

			AmazonSNSAsync amazonSNSClient = new AmazonSNSAsyncClient(awsCredentials);
			amazonSNSClient.setEndpoint(endpoint);
			clients.put(endpoint, amazonSNSClient);
		}

		return clients.get(endpoint);
	}
}
