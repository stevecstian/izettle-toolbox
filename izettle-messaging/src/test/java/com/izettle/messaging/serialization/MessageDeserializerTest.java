package com.izettle.messaging.serialization;

import static com.izettle.java.ResourceUtils.getResourceAsBytes;
import static org.junit.Assert.assertEquals;

import com.izettle.java.DateFormatCreator;
import com.izettle.java.TimeZoneId;
import com.izettle.java.UUIDFactory;
import com.izettle.messaging.TestMessage;
import com.izettle.messaging.TestMessageWithDate;
import com.izettle.messaging.TestMessageWithInstant;
import com.izettle.messaging.TestMessageWithUUID;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class MessageDeserializerTest {

    private MessageDeserializer<TestMessage> plaintextDeserializer;
    private MessageDeserializer<TestMessageWithUUID> plaintextWithUUIDDeserializer;
    private String plaintextMessage;
    private String messageSentThroughSNS;
    private String messageSentThroughSQS;
    private String messageSentDirectlyToSQS;

    @Before
    public void setup() throws IOException {
        plaintextDeserializer = new MessageDeserializer<>(TestMessage.class);
        plaintextWithUUIDDeserializer = new MessageDeserializer<>(TestMessageWithUUID.class);
        plaintextMessage = new String(getResourceAsBytes("example-message.json"));
        messageSentThroughSNS = new String(getResourceAsBytes("example-message-sent-with-messagepublisher-through-sns.json"));
        messageSentThroughSQS = new String(getResourceAsBytes("example-message-sent-with-messagepublisher-to-sqs.json"));
        messageSentDirectlyToSQS = new String(getResourceAsBytes("example-message-sent-with-messagequeueproducer-to-sqs.json")).trim();
    }

    @Test
    public void decryptUsingPlaintextDeserializerShouldDoNothing() throws Exception {
        String decrypted = plaintextDeserializer.decrypt(plaintextMessage);
        assertEquals(plaintextMessage, decrypted);
    }

    @Test
    public void deserializingPlaintextMessageShouldResultInValidObject() throws Exception {
        TestMessage msg = plaintextDeserializer.deserialize(plaintextMessage);
        assertEquals("message in a bottle", msg.getMessage());
    }

    @Test
    public void deserializingMessageWithRfc3339DateShouldParseDateCorrectly() throws Exception {
        // Arrange
        String json = "{\"date\":\"2001-12-23T03:05:06.123+0100\"}";

        // Act
        TestMessageWithDate msg = new MessageDeserializer<>(TestMessageWithDate.class).deserialize(json);

        // Assert
        String dateFieldAsString = DateFormatCreator.createDateAndTimeMillisFormatter(TimeZoneId.UTC)
                .format(msg.getDate());
        assertEquals("2001-12-23 02:05:06.123", dateFieldAsString);
    }

    @Test
    public void deserializingMessageWithRfc3339DateShouldParseInstantCorrectly() throws Exception {
        // Arrange
        String json = "{\"instant\":\"2001-12-23T03:05:06.123+0100\"}";

        // Act
        TestMessageWithInstant msg = new MessageDeserializer<>(TestMessageWithInstant.class).deserialize(json);

        // Assert
        String dateFieldAsString = DateTimeFormatter.ISO_INSTANT.withZone(TimeZoneId.UTC.toZoneId()).format(msg.getInstant());
        assertEquals("2001-12-23T02:05:06.123Z", dateFieldAsString);
    }

    @Test
    public void deserializingMessageWithNonRfcFormattedInstantShouldParseInstantCorrectly() throws Exception {
        // Arrange
        String json = "{\"instant\":\"2001-12-23T02:05:06.123Z\"}";

        // Act
        TestMessageWithInstant msg = new MessageDeserializer<>(TestMessageWithInstant.class).deserialize(json);

        // Assert
        String dateFieldAsString = DateTimeFormatter.ISO_INSTANT.withZone(TimeZoneId.UTC.toZoneId()).format(msg.getInstant());
        assertEquals("2001-12-23T02:05:06.123Z", dateFieldAsString);
    }

    @Test
    public void shouldRemoveSNSEnvelopeFromMessageSentWithMessagePublisherThroughSNS() throws Exception {
        String msg = MessageDeserializer.removeSnsEnvelope(messageSentThroughSNS);
        assertEquals("{\"amount\":3135,\"message\":\"MessagePublisher to SNS\","
            + "\"uuid1\":\"0SFwIEwSEeWQb_kt6mwGgg\",\"uuid2\":\"49c07050-7675-4a65-9e5e-e26d52146d2a\"}", msg);
    }

    @Test
    public void shouldRemoveSNSEnvelopeFromMessageSentWithMessagePublisherThroughSQS() throws Exception {
        String msg = MessageDeserializer.removeSnsEnvelope(messageSentThroughSQS);
        assertEquals("{\"amount\":3133,\"message\":\"MessagePublisher to SQS\"}", msg);
    }

    @Test
    public void shouldRemoveSNSEnvelopeFromMessageSentWithMessageQueueProducerToSQS() throws Exception {
        String msg = MessageDeserializer.removeSnsEnvelope(messageSentDirectlyToSQS);
        assertEquals("{\"amount\":3134,\"message\":\"MessageQueueProducer to SQS\"}", msg);
    }

    @Test
    public void shouldDeserializeMessageSentWithMessagePublisherThroughSNS() throws Exception {
        UUID uuid1 = UUIDFactory.parse("0SFwIEwSEeWQb_kt6mwGgg");
        UUID uuid2 = UUID.fromString("49c07050-7675-4a65-9e5e-e26d52146d2a");

        TestMessageWithUUID msg = plaintextWithUUIDDeserializer.deserialize(
            MessageDeserializer.removeSnsEnvelope(messageSentThroughSNS)
        );
        assertEquals("MessagePublisher to SNS", msg.getMessage());
        assertEquals(uuid1, msg.getUuid1());
        assertEquals(uuid2, msg.getUuid2());
    }

    @Test
    public void shouldDeserializeMessageSentWithMessagePublisherThroughSQS() throws Exception {
        TestMessage msg = plaintextDeserializer.deserialize(
            MessageDeserializer.removeSnsEnvelope(messageSentThroughSQS)
        );
        assertEquals("MessagePublisher to SQS", msg.getMessage());
    }

    @Test
    public void shouldDeserializeMessageSentWithMessageQueueProducerToSQS() throws Exception {
        TestMessage msg = plaintextDeserializer.deserialize(
            MessageDeserializer.removeSnsEnvelope(messageSentDirectlyToSQS)
        );
        assertEquals("MessageQueueProducer to SQS", msg.getMessage());
    }
}
