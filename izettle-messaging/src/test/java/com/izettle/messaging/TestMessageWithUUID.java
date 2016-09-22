package com.izettle.messaging;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestMessageWithUUID {
    private String message;
    private UUID uuid1;
    private UUID uuid2;

    public TestMessageWithUUID(String message) {
        this.message = message;
    }

    @JsonCreator
    public TestMessageWithUUID(
        @JsonProperty("message") String message,
        @JsonProperty("uuid1") UUID uuid1,
        @JsonProperty("uuid2")UUID uuid2) {
        this.message = message;
        this.uuid1 = uuid1;
        this.uuid2 = uuid2;
    }

    public String getMessage() {
        return message;
    }

    public UUID getUuid1() {
        return uuid1;
    }

    public void setUuid1(UUID uuid1) {
        this.uuid1 = uuid1;
    }

    public UUID getUuid2() {
        return uuid2;
    }

    public void setUuid2(UUID uuid2) {
        this.uuid2 = uuid2;
    }
}
