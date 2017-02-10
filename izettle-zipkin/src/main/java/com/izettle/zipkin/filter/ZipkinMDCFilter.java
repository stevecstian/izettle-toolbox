package com.izettle.zipkin.filter;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.IdConversion;
import com.twitter.zipkin.gen.Span;
import java.io.IOException;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import org.slf4j.MDC;

/**
 * Created by alberto on 2017-02-10.
 */

/*
* TODO: If/when taking the zipkin implementation further we should probably change the ZipkinEnhancedMDCFilter to
* execute
* separate from MDCFilter, and by doing that making ZipkinEnhancedMDCFilter execute directly after Zipkin trace/span
* has been initialized. By doing this zipkin trace id:s will part of logs that are created during
* authorization/authentication
* phase.
*
* NOTE: We should also set zipkin ID:s to MDC when consuming messages thorough message queues - see for example com.izettle
* .invoice.core.messaging.zipkin.TracedMessageHandler
 */

@Priority(Priorities.AUTHENTICATION + 1)
public class ZipkinMDCFilter implements ContainerRequestFilter, ContainerResponseFilter {

    // TODO: Probably only traceID should be needed in logs, so span/parentSpanId should be possible to remove
    public static final String MDC_TRACER_ID = "tracerID";
    public static final String MDC_SPAN_ID = "spanID";
    public static final String MDC_PARENT_ID = "parentID";

    private final Brave brave;

    public ZipkinMDCFilter(Brave brave) {
        this.brave = brave;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        Span span = getSpan();

        if (span != null) {
            if (span.getId() != 0) {
                MDC.put(MDC_SPAN_ID, IdConversion.convertToString(span.getId()));
            }
            if (span.getTrace_id() != 0) {
                MDC.put(MDC_TRACER_ID, IdConversion.convertToString(span.getTrace_id()));
            }
            if (span.getParent_id() != null) {
                MDC.put(MDC_PARENT_ID, IdConversion.convertToString(span.getParent_id()));
            }
        }
    }

    @Override
    public void filter(
        ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext
    ) throws IOException {

    }


    private Span getSpan() {
        if (brave.serverSpanThreadBinder().getCurrentServerSpan() != null) {
            return brave.serverSpanThreadBinder().getCurrentServerSpan().getSpan();
        }

        return null;
    }

}
