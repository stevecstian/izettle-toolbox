package com.izettle.zipkin.messaging;

/**
 * Created by alberto on 2017-02-10.
 *
 * Based on Martin Fridh's implementation
 */

import static com.izettle.java.ValueChecks.anyEmpty;
import static com.izettle.java.ValueChecks.empty;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.SpanId;
import com.izettle.messaging.MessagePublisher;
import com.izettle.messaging.MessagingException;
import com.izettle.messaging.serialization.DefaultMessageSerializer;
import com.izettle.messaging.serialization.MessageSerializer;
import java.util.Collection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin.Constants;

/**
 * Convenience class for using Amazon Simple Notification Service that
 * also reports spans to Zipkin.
 */
public class TracedPublisherService implements MessagePublisher {

    private static final Logger LOG = LoggerFactory.getLogger(TracedPublisherService.class);

    private final String topicArn;
    private final AmazonSNS amazonSNS;
    private final MessageSerializer messageSerializer;

    private final Optional<Brave> brave;

    public static TracedPublisherService nonEncryptedPublisherService(
        AmazonSNS client,
        final String topicArn,
        Optional<Brave> brave
    ) {
        return new TracedPublisherService(client, topicArn, new DefaultMessageSerializer(), brave);
    }

    private TracedPublisherService(
        AmazonSNS client,
        String topicArn,
        MessageSerializer messageSerializer,
        Optional<Brave> brave
    ) {
        if (anyEmpty(client, topicArn, messageSerializer, brave)) {
            throw new IllegalArgumentException(
                "None of client, topicArn, brave or messageSerializer can be empty!\n"
                    + "client = " + client + "\n"
                    + "topicArn = " + topicArn + "\n"
                    + "brave = " + brave + "\n"
                    + "messageSerializer = " + messageSerializer
            );
        }
        this.brave = brave;
        this.amazonSNS = client;
        this.topicArn = topicArn;
        this.messageSerializer = messageSerializer;
    }

    /**
     * Posts message to queue.
     *
     * @param message Message to post.
     * @param eventName Message subject (type of message).
     * @throws MessagingException Failed to post message.
     */
    @Override
    public <M> void post(M message, String eventName) throws MessagingException {

        LOG.info("About to post message {}, event {}, brave is {}", message, eventName, brave);

        if (empty(eventName)) {
            throw new MessagingException("Cannot publish message with empty eventName!");
        }

        SpanId spanId = null;
        try {
            if (message instanceof TracedEvent) {
                spanId = initializeSpan((TracedEvent) message);

                if (spanId != null) {
                    appendSpanInfoToMessage((TracedEvent) message, spanId);
                }
            }

            String jsonBody = messageSerializer.serialize(message);
            String encryptedBody = messageSerializer.encrypt(jsonBody);
            PublishRequest publishRequest = new PublishRequest(topicArn, encryptedBody, eventName);

            amazonSNS.publish(publishRequest);

            if (spanId != null) {
                finishSpan(spanId, null);
            }
        } catch (Exception e) {
            if (spanId != null) {
                finishSpan(spanId, e);
            }
            throw new MessagingException("Failed to publish message " + eventName, e);
        }
    }

    private void finishSpan(SpanId spanId, Exception e) {
        if (brave.isPresent()) {
            if (e != null) {
                brave.get().clientTracer().submitBinaryAnnotation(Constants.ERROR, e.getMessage());
            }

            brave.get().clientTracer().setClientReceived();
            LOG.info("Client Span finished {}", spanId.getSpanId());
        } else {
            LOG.info("Tracing disabled, won't finish nothing.");
        }

    }

    private SpanId initializeSpan(TracedEvent event) {
        if (brave.isPresent()) {
            SpanId spanId = brave.get().clientTracer().startNewSpan(event.getSpanName() + "-publish");

            if (spanId == null) { // send directly
                return null;
            }

            brave.get().clientTracer().setClientSent();
            brave.get().clientTracer().submitBinaryAnnotation("sns.sent", event.getEventName());

            LOG.info(
                "Client Span initialized for event={}",
                event.getEventName()
            );

            return spanId;
        } else {
            LOG.info("Tracing disabled, won't initialize span.");
            return null;
        }

    }

    private void appendSpanInfoToMessage(TracedEvent event, SpanId span) {
        event.setParentSpanId(span.getParentSpanId());
        event.setSpanId(span.getSpanId() != 0 ? span.getSpanId() : null);
        event.setTraceId(span.getTraceId() != 0 ? span.getTraceId() : null);
    }

    /**
     * Posts several messages to topic.
     *
     * @param messages Messages to post.
     * @param eventName Message subject (type of message).
     * @throws MessagingException Failed to post at least one of the messages.
     */
    @Override
    public <M> void postBatch(Collection<M> messages, String eventName) throws MessagingException {
        for (M message : messages) {
            post(message, eventName);
        }
    }

}

