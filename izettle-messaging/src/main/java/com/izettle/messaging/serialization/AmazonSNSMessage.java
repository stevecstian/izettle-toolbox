package com.izettle.messaging.serialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AmazonSNSMessage {
	
	@JsonProperty("Type")
	private String type;
	
	@JsonProperty("MessageId")
	private String messageId;
	
	@JsonProperty("Message")
	private String message;

	@JsonProperty("Subject")
	private String subject;

	public String getType() {
		return type;
	}

	public String getMessageId() {
		return messageId;
	}

	public String getMessage() {
		return message;
	}

	public String getSubject() {
		return subject;
	}
}