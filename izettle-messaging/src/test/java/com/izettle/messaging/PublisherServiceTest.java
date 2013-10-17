package com.izettle.messaging;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
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
		MessageQueueProducer<TestMessage> publisherService = PublisherService.nonEncryptedPublisherService(snsClient, "topicArn");

		// Act
		publisherService.post(message);

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
		MessageQueueProducer<TestMessage> publisherService = PublisherService.nonEncryptedPublisherService(snsClient, "topicArn", "ForcedEventName");

		// Act
		publisherService.post(message);

		// Assert
		ArgumentCaptor<PublishRequest> argumentCaptor = ArgumentCaptor.forClass(PublishRequest.class);
		verify(snsClient).publish(argumentCaptor.capture());
		assertEquals("topicArn", argumentCaptor.getValue().getTopicArn());
		assertEquals("ForcedEventName", argumentCaptor.getValue().getSubject());
		assertEquals("{\"message\":\"ad99bb4f\"}", argumentCaptor.getValue().getMessage());
	}
}
