package com.izettle.messaging;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory that creates Amazon SNS clients for specific endpoints. This factory should be used to ensure that only
 * one client is created per endpoint.
 */
public class AmazonSNSClientFactory {
	private static final Logger LOG = LoggerFactory.getLogger(AmazonSNSClientFactory.class);
	private static final Map<String, AmazonSNSClient> clients = new HashMap<>();

	/**
	 * Creates Amazon SNS client for given endpoint using the provided credentials.
	 *
	 * @param endpoint Amazon SNS endpoint.
	 * @param awsCredentials AWS credentials with access to the endpoint.
	 * @return Amazon SNS client.
	 */
	public static synchronized AmazonSNSClient getInstance(String endpoint, AWSCredentials awsCredentials) {

		LOG.info(String.format("Creating AWS client for endpoint %s", endpoint));

		if (!clients.containsKey(endpoint)) {
			AmazonSNSClient amazonSNSClient = new AmazonSNSClient(awsCredentials);
			amazonSNSClient.setEndpoint(endpoint);
			clients.put(endpoint, amazonSNSClient);
		}

		return clients.get(endpoint);
	}
}
