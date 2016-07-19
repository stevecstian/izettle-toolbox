package com.izettle.messaging.serialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AmazonSQSMessage {

    @JsonProperty("Body")
    private AmazonSNSMessage body;

    @JsonProperty("ReceiptHandle")
    private String receiptHandle;

    @JsonProperty("MessageId")
    private UUID messageId;

    @JsonProperty("MD5OfBody")
    private String md5OfBody;

    public AmazonSQSMessage() {
    }

    public AmazonSQSMessage(AmazonSNSMessage body, String receiptHandle) {
        this.body = body;
        this.receiptHandle = receiptHandle;
    }

    public AmazonSNSMessage getBody() {
        return body;
    }

    public String getReceiptHandle() {
        return receiptHandle;
    }

    public UUID getMessageId() {
        return messageId;
    }

    public String getMd5OfBody() {
        return md5OfBody;
    }
}
