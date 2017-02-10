package com.izettle.zipkin.messaging;

/**
 * Created by alberto on 2017-02-10.
 *
 * Based on Martin Fridh's implementation
 */

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.SpanId;
import com.izettle.messaging.handler.MessageHandler;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wrapper around MessageHandler that reports spans to a Zipkin.
 */
public class TracedMessageHandler<M> implements MessageHandler<M> {

    private static final Logger LOG = LoggerFactory.getLogger(TracedMessageHandler.class);

    private final MessageHandler<M> actualHandler;
    private final Brave brave;

    public TracedMessageHandler(MessageHandler<M> actualHandler, Brave brave) {
        this.actualHandler = actualHandler;

        this.brave = brave;
    }

    @Override
    public void handle(M message) throws Exception {

        // TODO: we should set traceId to log4j MDC when consuming messages. Also sett com.izettle.invoice.filter
        // .ZipkinEnhancedMDCFilter

        if (message instanceof TracedEvent) {
            initializeTracing((TracedEvent) message);
        }

        actualHandler.handle(message);

        if (message instanceof TracedEvent) {
            finalizeTracing();
        }
    }

    private void finalizeTracing() {
        LOG.info("Finalizing tracing");

        brave.serverTracer().setServerSend();
    }

    private void initializeTracing(TracedEvent event) {
        if (event.getSpanId() != null) {
            SpanId spanId = generateChildSpandId(event);
            LOG.info(
                "Initializing server trace for event={}",
                event.getEventName()
            );

            brave.serverTracer().setStateCurrentTrace(spanId, event.getSpanName() + "-consume");
            brave.serverTracer().setServerReceived();
            brave.serverTracer().submitBinaryAnnotation("sqs.received", event.getEventName());

        } else {
            LOG.info("Received no span state.");
        }

    }

    private SpanId generateChildSpandId(TracedEvent event) {
        SpanId.Builder spanIdBuilder = SpanId.builder()
            .spanId(new SecureRandom().nextLong())
            .parentId(event.getSpanId());

        if (event.getTraceId() != null) {
            spanIdBuilder = spanIdBuilder.traceId(event.getTraceId());
        }

        return spanIdBuilder.build();
    }
}

