package com.izettle.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestMessage {
    private String message;

    public TestMessage() {
    }

    public TestMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
