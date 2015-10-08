package com.izettle.messaging.handler;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.izettle.messaging.RetryableMessageHandlerException;

/**
 * A wrapper around MessageHandler that reports metrics to a MetricsRegistry.
 *
 * NOTE: This depends on Dropwizard Metrics 3.1.1
 */
public class MeteredMessageHandler<M> implements MessageHandler<M> {
    private final MessageHandler<M> actualHandler;
    private final Meter meterAll;
    private final Meter meterError;
    private final Meter meterRetryable;
    private final Timer timer;

    public MeteredMessageHandler(MessageHandler<M> actualHandler, MetricRegistry metricRegistry, String metricsName) {
        this.actualHandler = actualHandler;
        this.meterAll = metricRegistry.meter(metricsName + ".all");
        this.meterRetryable = metricRegistry.meter(metricsName + ".retryable");
        this.meterError = metricRegistry.meter(metricsName + ".error");
        timer = metricRegistry.timer(metricsName);
    }

    @Override
    public void handle(M message) throws Exception {

        Timer.Context timerContext = timer.time();
        try {
            meterAll.mark();
            actualHandler.handle(message);
        } catch (RetryableMessageHandlerException e) {
            meterRetryable.mark();
            throw e;
        } catch (Exception e) {
            meterError.mark();
            throw e;
        } finally {
            timerContext.stop();
        }
    }
}
