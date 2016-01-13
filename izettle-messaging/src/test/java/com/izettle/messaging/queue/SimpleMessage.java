package com.izettle.messaging.queue;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SimpleMessage {

    @JsonProperty
    public int amount;

    public SimpleMessage() {
    }

    public SimpleMessage(int amount) {
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
