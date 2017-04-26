package com.izettle.messaging;

import static com.izettle.messaging.AmazonSNSClientFactory.DEFAULT_PROVIDER_CHAIN;
import static com.izettle.messaging.AmazonSNSClientFactory.determineRegion;
import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AmazonSNSClientFactoryTest {

    private final String endpoint;
    private final String region;

    @Parameters
    public static Collection<String[]> endpoints() throws Exception {
        return Arrays.asList(new String[][]{
            {"https://sns.eu-west-1.amazonaws.com", "eu-west-1"},
            {"https://sns.eu-central-1.amazonaws.com", "eu-central-1"},
            {"http://yopa:47196/", "eu-west-1"}
        });

    }

    public AmazonSNSClientFactoryTest(String endpoint, String region) {
        this.endpoint = endpoint;
        this.region = region;
    }

    @Test
    public void test() {
        assertThat(determineRegion(endpoint)).isEqualTo(region);
    }

    @Test
    public void testBuilderBuildsWithEmptyCredentials() {
        assertThat(AmazonSNSClientFactory.builder(endpoint, null)).isNotNull();
    }

    @Test
    public void testBuilderBuildsWithCredentials() {
        AmazonSNSAsyncClientBuilder builder = AmazonSNSClientFactory.builder(
            endpoint,
            new BasicAWSCredentials("accessKey", "secretKey")
        );
        assertThat(builder).isNotNull();
        assertThat(builder.getCredentials()).isEqualTo(DEFAULT_PROVIDER_CHAIN);

    }


}