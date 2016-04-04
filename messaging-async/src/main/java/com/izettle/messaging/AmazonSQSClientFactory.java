package com.izettle.messaging;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;

/**
 * Factory that creates Amazon SQS clients for specific endpoints.
 */
public class AmazonSQSClientFactory {

    /**
     * Creates Amazon SQS client for given endpoint using the provided credentials.
     *
     * @param endpoint Amazon SQS endpoint.
     * @return Amazon SQS client.
     */
    public static AmazonSQSAsync createInstance(String endpoint) {
        return createInstance(endpoint, null);
    }

    /**
     * Creates Amazon SQS client for given endpoint using the provided credentials.
     *
     * @param endpoint Amazon SQS endpoint.
     * @param accessKey AWS credentials with access to the endpoint, or null to use default aws credentials.
     * @param secretKey AWS credentials with access to the endpoint.
     * @return Amazon SQS client.
     */
    public static AmazonSQSAsync createInstance(String endpoint, String accessKey, String secretKey) {
        return createInstance(endpoint, AWSCredentialsWrapper.getCredentials(accessKey, secretKey));
    }

    /**
     * Creates Amazon SQS client for given endpoint using the provided credentials.
     *
     * @param endpoint Amazon SQS endpoint.
     * @param awsCredentials AWS credentials with access to the endpoint, or null to use default aws credentials.
     * @return Amazon SQS client.
     */
    public static AmazonSQSAsync createInstance(String endpoint, AWSCredentials awsCredentials) {
        AmazonSQSAsync client = createInstance(awsCredentials);
        client.setEndpoint(endpoint);
        return client;
    }

    /**
     * Creates Amazon SQS client for given endpoint using the provided credentials.
     *
     * @param awsCredentials AWS credentials with access to the endpoint, or null to use default aws credentials.
     * @return Amazon SQS client.
     */
    private static AmazonSQSAsync createInstance(AWSCredentials awsCredentials) {
        if (awsCredentials == null) {
            return new AmazonSQSAsyncClient();
        } else {
            return new AmazonSQSAsyncClient(awsCredentials);
        }
    }
}
