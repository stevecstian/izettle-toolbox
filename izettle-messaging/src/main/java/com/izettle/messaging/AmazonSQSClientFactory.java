package com.izettle.messaging;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.ClientConfigurationFactory;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import java.util.concurrent.Executors;

/**
 * Factory that creates Amazon SQS clients for specific endpoints.
 */
public class AmazonSQSClientFactory {

    /**
     * Client configuration factory providing ClientConfigurations tailored to this client
     */
    private static final ClientConfigurationFactory CONFIG_FACTORY = new ClientConfigurationFactory();

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
        // default is 3 retries
        ClientConfiguration clientConfiguration = CONFIG_FACTORY.getConfig().withMaxErrorRetry(6);
        if (awsCredentials == null) {
            return new AmazonSQSAsyncClient(clientConfiguration);
        } else {
            // 50 is copied from AmazonSQSAsyncClient.DEFAULT_THREAD_POOL_SIZE
            return new AmazonSQSAsyncClient(awsCredentials, clientConfiguration, Executors.newFixedThreadPool(50));
        }
    }
}
