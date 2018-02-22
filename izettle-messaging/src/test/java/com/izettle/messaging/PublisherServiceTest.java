package com.izettle.messaging;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.izettle.messaging.serialization.MessageSerializer;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

public class PublisherServiceTest {
    private final AmazonSNSClient snsClient = mock(AmazonSNSClient.class);
    @Rule
    public ExpectedException thrown = ExpectedException.none();

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
        final MessagePublisher messagePublisher = PublisherService.nonEncryptedPublisherService(
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

    @Test
    public void itShouldFailToConstructWithEmptySNSClient() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(startsWith("None of client, topicArn or messageSerializer can be empty!"));
        PublisherService.nonEncryptedPublisherService(null, "topicArn");
    }

    @Test
    public void itShouldFailToConstructWithEmptyTopicArn() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(startsWith("None of client, topicArn or messageSerializer can be empty!"));
        PublisherService.nonEncryptedPublisherService(snsClient, "");
    }

    @Test
    public void itShouldFailToConstructWithEmptyMessageSerializer() throws Exception {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(startsWith("None of client, topicArn or messageSerializer can be empty!"));
        PublisherService.nonEncryptedPublisherService(snsClient, "topicArn", null);
    }

    @Test
    public void shouldSetMessageAttributesWhenPostingToSNS() throws Exception {

        // Arrange
        TestMessage message = new TestMessage("ad99bb4f");
        MessagePublisher publisherService = PublisherService.nonEncryptedPublisherService(snsClient, "topicArn");
        Map<String, String> attributes = new HashMap<>();
        String key = "attr";
        String value = "value";
        attributes.put(key, value);

        // Act
        publisherService.post(message, TestMessage.class.getName(), attributes);

        // Assert
        ArgumentCaptor<PublishRequest> argumentCaptor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(argumentCaptor.capture());
        Map<String, MessageAttributeValue> messageAttributes = argumentCaptor.getValue().getMessageAttributes();
        assertNotNull(messageAttributes);
        assertTrue(messageAttributes.containsKey(key));
        MessageAttributeValue messageAttribute = messageAttributes.get(key);
        assertEquals("String", messageAttribute.getDataType());
        assertEquals(value, messageAttribute.getStringValue());
    }
    @Test
    public void shouldFailIfYouAddToManyAttributesWhenPostingToSNS() throws Exception {

        // Arrange
        thrown.expect(MessagingException.class);
        thrown.expectMessage(startsWith("Cannot publish message with more than 10 attributes!"));
        TestMessage message = new TestMessage("ad99bb4f");
        MessagePublisher publisherService = PublisherService.nonEncryptedPublisherService(snsClient, "topicArn");
        Map<String, String> attributes = new HashMap<>();
        IntStream.range(0, 11).forEach(num-> attributes.put("attr" + num, "value" + num));

        // Act
        publisherService.post(message, TestMessage.class.getName(), attributes);
    }
}
