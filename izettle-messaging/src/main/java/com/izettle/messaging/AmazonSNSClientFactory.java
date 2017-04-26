package com.izettle.messaging;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import java.util.Arrays;

/**
 * Factory that creates Amazon SNS clients for specific endpoints.
 */
public class AmazonSNSClientFactory {

    static final DefaultAWSCredentialsProviderChain DEFAULT_PROVIDER_CHAIN =
        DefaultAWSCredentialsProviderChain.getInstance();

    /**
     * Creates Amazon SNS client for given endpoint using the provided credentials.
     *
     * @param endpoint Amazon SNS endpoint.
     * @return Amazon SNS client.
     */
    public static AmazonSNSAsync createInstance(String endpoint) {
        return builder(endpoint, null).build();
    }

    /**
     * Creates Amazon SNS client for given endpoint using the provided credentials.
     *
     * @param endpoint Amazon SNS endpoint.
     * @param accessKey AWS credentials with access to the endpoint, or null to use default aws credentials.
     * @param secretKey AWS credentials with access to the endpoint.
     * @return Amazon SNS client.
     */
    @Deprecated
    public static AmazonSNSAsync createInstance(String endpoint, String accessKey, String secretKey) {
        return builder(
            endpoint,
            AWSCredentialsWrapper.getCredentials(accessKey, secretKey)
        ).build();
    }

    /**
     * Creates Amazon SNS client for given endpoint using the provided credentials.
     *
     * @param endpoint Amazon SNS endpoint.
     * @param awsCredentials AWS credentials with access to the endpoint, or null to use default aws credentials.
     * @return Amazon SNS client.
     */
    public static AmazonSNSAsync createInstance(String endpoint, AWSCredentials awsCredentials) {
        return builder(endpoint, awsCredentials).build();
    }

    /**
     * Extract region from endpoint
     *
     * @param endpoint
     * @return region if endpoint is standard
     */
    static Regions determineRegion(String endpoint) {
        return Arrays.stream(Regions.values())
            .filter(r -> endpoint.contains(r.getName()))
            .findFirst()
            .orElse(Regions.EU_WEST_1);
    }

    static AmazonSNSAsyncClientBuilder builder(
        String endpoint,
        AWSCredentials awsCredentials
    ) {
        final AmazonSNSAsyncClientBuilder builder = AmazonSNSAsyncClientBuilder.standard()
            .withEndpointConfiguration(new EndpointConfiguration(endpoint, determineRegion(endpoint).getName()));

        // add credentials then default provider chain
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
