package com.izettle.messaging;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.EmptyBatchRequestException;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.izettle.messaging.serialization.AmazonSNSMessage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class QueueServiceTest {

	private QueueService<TestMessage> queueService;
	private AmazonSQS mockAmazonSQS = mock(AmazonSQS.class);

	@Before
	public final void before() throws Exception {
		queueService = new QueueService<>(TestMessage.class, "queueUrl", mockAmazonSQS);
	}

	@Test
	public void addAndDeleteUserMessageShouldWork() throws Exception {
		when(mockAmazonSQS.sendMessage(any(SendMessageRequest.class))).thenReturn(mock(SendMessageResult.class));
		ReceiveMessageResult receiveMessageResult = mock(ReceiveMessageResult.class);
		Message message = mock(Message.class);
		when(message.getBody()).thenReturn("{}");
		when(receiveMessageResult.getMessages()).thenReturn(Arrays.asList(message));
		when(mockAmazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveMessageResult);

		TestMessage testMessage = new TestMessage("Hello!");

		queueService.post(testMessage);

		List<MessageWrapper<TestMessage>> receivedMessages = queueService.poll();

		assertThat(receivedMessages).hasSize(1);

		when(mockAmazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(mock(ReceiveMessageResult.class));

		queueService.delete(receivedMessages.get(0));
		receivedMessages = queueService.poll();
		assertThat(receivedMessages).isEmpty();
	}

	@Test
	public void shouldNotSendEmptyBatch() throws Exception {
		ArgumentCaptor<SendMessageBatchRequest> captor = ArgumentCaptor.forClass(SendMessageBatchRequest.class);

		SendMessageBatchRequest emptyRequest = new SendMessageBatchRequest("queue-url-can-be-anything", new ArrayList<SendMessageBatchRequestEntry>());
		when(mockAmazonSQS.sendMessageBatch(emptyRequest)).thenThrow(new EmptyBatchRequestException("Empty yo"));

		queueService.postBatch(messageBatch(10));

		verify(mockAmazonSQS, times(1)).sendMessageBatch(captor.capture());
		assertThat(captor.getValue().getEntries()).hasSize(10);

		queueService.postBatch(messageBatch(20));

		verify(mockAmazonSQS, times(3)).sendMessageBatch(any(SendMessageBatchRequest.class));
		queueService.postBatch(messageBatch(11));

		verify(mockAmazonSQS, times(5)).sendMessageBatch(captor.capture());
		assertThat(captor.getValue().getEntries()).hasSize(1);

		queueService.postBatch(messageBatch(9));

		// capture sixth interaction
		verify(mockAmazonSQS, times(6)).sendMessageBatch(captor.capture());
		assertThat(captor.getValue().getEntries()).hasSize(9);
	}

	private Map<String, TestMessage> messageBatch(int size) {
		Map<String, TestMessage> userBatch = new HashMap<>();
		for (int i = 0; i < size; i++) {
			final TestMessage testMessage = new TestMessage("test" + i);
			userBatch.put(testMessage.getMessage(), testMessage);
		}
		return userBatch;
	}

	@Test
	public void addAndDeleteBatchMessagesShouldWork() throws Exception {

		ReceiveMessageResult receiveMessageResult = mock(ReceiveMessageResult.class);
		Message message = mock(Message.class);
		when(message.getBody()).thenReturn("{}");
		when(receiveMessageResult.getMessages()).thenReturn(Arrays.asList(message, message));
		when(mockAmazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(receiveMessageResult);

		final TestMessage user1 = new TestMessage("Hello");
		final TestMessage user2 = new TestMessage("world!");

		queueService.postBatch(new HashMap<String, TestMessage>() {
			{
				put(user1.getMessage(), user1);
				put(user2.getMessage(), user2);
			}
		});

		List<MessageWrapper<TestMessage>> receivedMessages = queueService.poll();

		assertEquals(2, receivedMessages.size());

		when(mockAmazonSQS.receiveMessage(any(ReceiveMessageRequest.class))).thenReturn(mock(ReceiveMessageResult.class));

		queueService.delete(receivedMessages.get(0));
		queueService.delete(receivedMessages.get(1));
		receivedMessages = queueService.poll();
		assertEquals(0, receivedMessages.size());
	}

	@Test
	public void postBatchAsSNSMessagesShouldSendMessagesWithSNSEnvelope() throws Exception {

		// Arrange
		ArgumentCaptor<SendMessageBatchRequest> captor = ArgumentCaptor.forClass(SendMessageBatchRequest.class);

		// Act
		queueService.postBatchAsSNSMessages(
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
		queueService.postAsSNSMessage(new TestMessage("Hello"), "subject");

		// Assert
		verify(mockAmazonSQS).sendMessageBatch(any(SendMessageBatchRequest.class));
	}
}
