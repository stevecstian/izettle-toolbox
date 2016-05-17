package com.izettle.messaging;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.ClientConfigurationFactory;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClient;
import java.util.concurrent.Executors;

/**
 * Factory that creates Amazon SNS clients for specific endpoints.
 */
public class AmazonSNSClientFactory {

    /**
     * Client configuration factory providing ClientConfigurations tailored to this client
     */
    private static final ClientConfigurationFactory CONFIG_FACTORY = new ClientConfigurationFactory();

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
        // default is 3 retries
        ClientConfiguration clientConfiguration = CONFIG_FACTORY.getConfig().withMaxErrorRetry(6);
        if (awsCredentials == null) {
            return new AmazonSNSAsyncClient(clientConfiguration);
        } else {
            // 50 is copied from AmazonSNSAsyncClient.DEFAULT_THREAD_POOL_SIZE
            return new AmazonSNSAsyncClient(awsCredentials, clientConfiguration, Executors.newFixedThreadPool(50));
        }
    }
}
