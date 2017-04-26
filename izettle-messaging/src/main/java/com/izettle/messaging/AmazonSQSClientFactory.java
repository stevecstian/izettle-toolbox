package com.izettle.messaging;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import java.util.Arrays;

/**
 * Factory that creates Amazon SQS clients for specific endpoints.
 */
public class AmazonSQSClientFactory {

    private static final DefaultAWSCredentialsProviderChain DEFAULT_PROVIDER_CHAIN =
        DefaultAWSCredentialsProviderChain.getInstance();

    /**
     * Creates Amazon SQS client for given endpoint using the provided credentials.
     *
     * @param endpoint Amazon SQS endpoint.
     * @return Amazon SQS client.
     */
    public static AmazonSQSAsync createInstance(String endpoint) {
        return builder(endpoint, null).build();
    }

    /**
     * Creates Amazon SQS client for given endpoint using the provided credentials.
     *
     * @param endpoint Amazon SQS endpoint.
     * @param accessKey AWS credentials with access to the endpoint, or null to use default aws credentials.
     * @param secretKey AWS credentials with access to the endpoint.
     * @return Amazon SQS client.
     */
    @Deprecated
    public static AmazonSQSAsync createInstance(String endpoint, String accessKey, String secretKey) {
        return builder(endpoint, AWSCredentialsWrapper.getCredentials(accessKey, secretKey)).build();
    }

    /**
     * Creates Amazon SQS client for given endpoint using the provided credentials.
     *
     * @param endpoint Amazon SQS endpoint.
     * @param awsCredentials AWS credentials with access to the endpoint, or null to use default aws credentials.
     * @return Amazon SQS client.
     */
    public static AmazonSQSAsync createInstance(String endpoint, AWSCredentials awsCredentials) {
        return builder(endpoint, awsCredentials).build();
    }

    /**
     * Extract region from endpoint
     *
     * @param endpoint
     * @return region or empty string if no region found
     */
    static String determineRegion(String endpoint) {
        return Arrays.stream(Regions.values())
            .filter(r -> endpoint.contains(r.getName()))
            .findFirst()
            .orElse(Regions.EU_WEST_1)
            .getName();
    }

    static AmazonSQSAsyncClientBuilder builder(
        String endpoint,
        AWSCredentials awsCredentials
    ) {
        AmazonSQSAsyncClientBuilder builder =
            AmazonSQSAsyncClientBuilder.standard()
                .withEndpointConfiguration(new EndpointConfiguration(
                    endpoint,
                    determineRegion(endpoint)
                ));
        if (awsCredentials != null) {
            return builder
                .withCredentials(
                    new AWSCredentialsProviderChain(
                        new AWSStaticCredentialsProvider(awsCredentials),
                        DEFAULT_PROVIDER_CHAIN
                    ));
        }

        return builder;
    }
}
