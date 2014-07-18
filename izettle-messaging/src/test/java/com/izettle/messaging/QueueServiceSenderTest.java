package com.izettle.messaging;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.EmptyBatchRequestException;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izettle.messaging.serialization.AmazonSNSMessage;
import com.izettle.messaging.serialization.MessageSerializer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class QueueServiceSenderTest {

	private MessageQueueProducer<TestMessage> messageQueueProducer;
	private MessagePublisher messagePublisher;
	private AmazonSQS mockAmazonSQS = mock(AmazonSQS.class);
	private static final String subject = "subject";

	@Before
	public final void before() throws Exception {
		messageQueueProducer = QueueServiceSender.nonEncryptedMessageQueueProducer("queueUrl", mockAmazonSQS);
		messagePublisher = QueueServiceSender.nonEncryptedMessagePublisher("queueUrl", mockAmazonSQS);
	}

	@Test
	public void postMessageShouldWork() throws Exception {
		when(mockAmazonSQS.sendMessage(any(SendMessageRequest.class))).thenReturn(mock(SendMessageResult.class));
		messageQueueProducer.post(new TestMessage("Hello!"));
		verify(mockAmazonSQS, times(1)).sendMessage(any(SendMessageRequest.class));
	}

	@Test
	public void shouldNotSendEmptyBatch() throws Exception {
		ArgumentCaptor<SendMessageBatchRequest> captor = ArgumentCaptor.forClass(SendMessageBatchRequest.class);

		SendMessageBatchRequest emptyRequest = new SendMessageBatchRequest("queue-url-can-be-anything", new ArrayList<SendMessageBatchRequestEntry>());
		when(mockAmazonSQS.sendMessageBatch(emptyRequest)).thenThrow(new EmptyBatchRequestException("Empty yo"));

		messagePublisher.postBatch(messageBatch(10), subject);
		verify(mockAmazonSQS, times(1)).sendMessageBatch(captor.capture());
		assertThat(captor.getValue().getEntries()).hasSize(10);

		messagePublisher.postBatch(messageBatch(20), subject);
		verify(mockAmazonSQS, times(3)).sendMessageBatch(any(SendMessageBatchRequest.class));

		messagePublisher.postBatch(messageBatch(11), subject);
		verify(mockAmazonSQS, times(5)).sendMessageBatch(captor.capture());
		assertThat(captor.getValue().getEntries()).hasSize(1);

		messagePublisher.postBatch(messageBatch(9), subject);
		verify(mockAmazonSQS, times(6)).sendMessageBatch(captor.capture());
		assertThat(captor.getValue().getEntries()).hasSize(9);
	}

	private Collection<TestMessage> messageBatch(int size) {
		List<TestMessage> result = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			result.add(new TestMessage("test" + i));
		}
		return result;
	}

	@Test
	public void postBatchShouldSendMessagesWithSNSEnvelope() throws Exception {

		// Arrange
		ArgumentCaptor<SendMessageBatchRequest> captor = ArgumentCaptor.forClass(SendMessageBatchRequest.class);

		// Act
		messagePublisher.postBatch(
				Arrays.asList(
						new TestMessage("Hello"), new TestMessage("world")
				), "subject"
		);

		// Assert
		verify(mockAmazonSQS).sendMessageBatch(captor.capture());

		SendMessageBatchRequest sendMessageBatchRequest = captor.getValue();
		assertThat(sendMessageBatchRequest.getQueueUrl()).isEqualTo("queueUrl");

		List<SendMessageBatchRequestEntry> entries = sendMessageBatchRequest.getEntries();
		assertThat(entries.size()).isEqualTo(2);

		ObjectMapper mapper = new ObjectMapper();
		AmazonSNSMessage msg1 = mapper.readValue(entries.get(0).getMessageBody(), AmazonSNSMessage.class);
		assertThat(msg1.getSubject()).isEqualTo("subject");
		assertThat(msg1.getMessage()).isEqualTo("{\"message\":\"Hello\"}");

		AmazonSNSMessage msg2 = mapper.readValue(entries.get(1).getMessageBody(), AmazonSNSMessage.class);
		assertThat(msg2.getSubject()).isEqualTo("subject");
		assertThat(msg2.getMessage()).isEqualTo("{\"message\":\"world\"}");
	}

	@Test
	public void postAsSNSMessageShouldSendMessagesWithSNSEnvelope() throws Exception {

		// Act
		messagePublisher.post(new TestMessage("Hello"), "subject");

		// Assert
		verify(mockAmazonSQS).sendMessageBatch(any(SendMessageBatchRequest.class));
	}

	@Test
	public void itShouldUseCallerSpecifiedMessageSerializer() throws Exception {
		// Arrange
		final TestMessage testMessage = new TestMessage("Hello");
		final String serializedMessage = "{\"msg\":\"world\"}";
		final MessageSerializer serializer = mock(MessageSerializer.class);
		when(serializer.serialize(testMessage)).thenReturn(serializedMessage);
		final MessagePublisher publisher = QueueServiceSender.nonEncryptedMessagePublisher(
				"test",
				mockAmazonSQS,
				serializer
		);

		// Act
		publisher.post(testMessage, "subject");

		// Assert
		verify(mockAmazonSQS).sendMessageBatch(any(SendMessageBatchRequest.class));
		verify(serializer).serialize(testMessage);
		verify(serializer).encrypt(serializedMessage);
	}

	@Test(expected = IllegalArgumentException.class)
	public void itShouldFailToConstructWithEmptyQueueUrl() throws Exception {
		QueueServiceSender.nonEncryptedMessageQueueProducer(null, mockAmazonSQS);
	}

	@Test(expected = IllegalArgumentException.class)
	public void itShouldFailToConstructWithEmptySQSClient() throws Exception {
		QueueServiceSender.nonEncryptedMessageQueueProducer("test.url", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void itShouldFailToConstructWithEmptyMessageSerializer() throws Exception {
		QueueServiceSender.nonEncryptedMessageQueueProducer("test.url", mockAmazonSQS, null);
	}
}
