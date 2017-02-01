package com.izettle.messaging;

import java.time.Instant;

public class TestMessageWithInstant {
    private Instant instant;

    public TestMessageWithInstant() {
    }

    public TestMessageWithInstant(Instant instant) {
        this.instant = instant;
    }

    public Instant getInstant() {
        return instant;
    }
}
