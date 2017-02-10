package com.izettle.zipkin.messaging;

import com.fasterxml.jackson.annotation.JsonInclude;

// TODO: Only hackweek level of quality here. Trace info should likely better match
// standard ways of propagating it, possibly by encoding ids to hex strings and also
// to propagate debyg/tracing-enabled flags (see section "Communicating trace information")
// at http://zipkin.io/pages/instrumenting.html
@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class TracedEvent implements Event {

    private Long traceId;
    private Long parentSpanId;
    private Long spanId;

    public Long getTraceId() {
        return traceId;
    }

    public void setTraceId(Long traceId) {
        this.traceId = traceId;
    }

    public Long getSpanId() {
        return spanId;
    }

    public void setSpanId(Long spanId) {
        this.spanId = spanId;
    }

    public Long getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(Long parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getSpanName() {
        return "event-" + getEventName();
    }
}