package com.izettle.messaging;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;

/**
 * Factory that creates Amazon SNS clients for specific endpoints.
 */
public class AmazonSNSClientFactory {

	/**
	 * Creates Amazon SNS client for given endpoint using the provided credentials.
	 *
	 * @param endpoint Amazon SNS endpoint.
	 * @return Amazon SNS client.
	 */
	public static AmazonSNSAsync createInstance(String endpoint) {
		return createInstance(endpoint, null);
	}

	/**
	 * Creates Amazon SNS client for given endpoint using the provided credentials.
	 *
	 * @param endpoint Amazon SNS endpoint.
	 * @param accessKey AWS credentials with access to the endpoint, or null to use default aws credentials.
	 * @param secretKey AWS credentials with access to the endpoint.
	 * @return Amazon SNS client.
	 */
	public static AmazonSNSAsync createInstance(String endpoint, String accessKey, String secretKey) {
		return createInstance(endpoint, AWSCredentialsWrapper.getCredentials(accessKey, secretKey));
	}

	/**
	 * Creates Amazon SNS client for given endpoint using the provided credentials.
	 *
	 * @param endpoint Amazon SNS endpoint.
	 * @param awsCredentials AWS credentials with access to the endpoint, or null to use default aws credentials.
	 * @return Amazon SNS client.
	 */
	public static AmazonSNSAsync createInstance(String endpoint, AWSCredentials awsCredentials) {
		AmazonSNSAsync client = createInstance(awsCredentials);
		client.setEndpoint(endpoint);
		return client;
	}

	/**
	 * Creates Amazon SNS client for given endpoint using the provided credentials.
	 *
	 * @param awsCredentials AWS credentials with access to the endpoint, or null to use default aws credentials.
	 * @return Amazon SNS client.
	 */
	private static AmazonSNSAsync createInstance(AWSCredentials awsCredentials) {
		if (awsCredentials == null) {
			return new AmazonSNSAsyncClient();
		} else {
			return new AmazonSNSAsyncClient(awsCredentials);
		}
	}
}
