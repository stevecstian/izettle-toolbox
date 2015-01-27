package com.izettle.messaging;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.izettle.messaging.serialization.MessageSerializer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PublisherServiceTest {
    private final AmazonSNSClient snsClient = mock(AmazonSNSClient.class);

    @Before
    public final void before() throws Exception {
        when(snsClient.publish(any(PublishRequest.class))).thenReturn(mock(PublishResult.class));
    }

    @Test
    public void shouldUseMessageTypeAsSubjectWhenPostingToSNS() throws Exception {

        // Arrange
        TestMessage message = new TestMessage("ad99bb4f");
        MessagePublisher publisherService = PublisherService.nonEncryptedPublisherService(snsClient, "topicArn");

        // Act
        publisherService.post(message, TestMessage.class.getName());

        // Assert
        ArgumentCaptor<PublishRequest> argumentCaptor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(argumentCaptor.capture());
        assertEquals("topicArn", argumentCaptor.getValue().getTopicArn());
        assertEquals(TestMessage.class.getName(), argumentCaptor.getValue().getSubject());
        assertEquals("{\"message\":\"ad99bb4f\"}", argumentCaptor.getValue().getMessage());
    }

    @Test
    public void shouldUseSpecifiedEventNameAsSubjectWhenPostingToSNS() throws Exception {

        // Arrange
        TestMessage message = new TestMessage("ad99bb4f");
        MessagePublisher publisherService = PublisherService.nonEncryptedPublisherService(snsClient, "topicArn");

        // Act
        publisherService.post(message, "ForcedEventName");

        // Assert
        ArgumentCaptor<PublishRequest> argumentCaptor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(argumentCaptor.capture());
        assertEquals("topicArn", argumentCaptor.getValue().getTopicArn());
        assertEquals("ForcedEventName", argumentCaptor.getValue().getSubject());
        assertEquals("{\"message\":\"ad99bb4f\"}", argumentCaptor.getValue().getMessage());
    }

    @Test
    public void itShouldUseTheSuppliedMessageSerializerForEncryptionAndSerializer() throws Exception {

        // Arrange
        final TestMessage message = new TestMessage("ad99bb4f");
        final MessageSerializer messageSerializer = mock(MessageSerializer.class);
        final String serializedMessage = "{\"message\":\"serialized message\"}";
        final String encryptedMessage = "{\"message\":\"encrypted message\"}";
        when(messageSerializer.serialize(message)).thenReturn(serializedMessage);
        when(messageSerializer.encrypt(serializedMessage)).thenReturn(encryptedMessage);
        final MessagePublisher messagePublisher  = PublisherService.nonEncryptedPublisherService(
                snsClient,
                "topicArn",
                messageSerializer
        );

        // Act
        messagePublisher.post(message, "TestName");

        //Verify
        verify(messageSerializer).serialize(message);
        verify(messageSerializer).encrypt(serializedMessage);

        ArgumentCaptor<PublishRequest> argumentCaptor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(argumentCaptor.capture());
        assertEquals("topicArn", argumentCaptor.getValue().getTopicArn());
        assertEquals("TestName", argumentCaptor.getValue().getSubject());
        assertEquals(encryptedMessage, argumentCaptor.getValue().getMessage());
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldFailToConstructWithEmptySNSClient() throws Exception {
        PublisherService.nonEncryptedPublisherService(null, "topicArn");
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldFailToConstructWithEmptyTopicArn() throws Exception {
        PublisherService.nonEncryptedPublisherService(snsClient, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void itShouldFailToConstructWithEmptyMessageSerializer() throws Exception {
        PublisherService.nonEncryptedPublisherService(snsClient, "topicArn", null);
    }
}
