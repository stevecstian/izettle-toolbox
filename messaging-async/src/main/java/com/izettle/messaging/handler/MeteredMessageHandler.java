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
    private final Meter meterError;
    private final Meter meterRetryable;
    private final Timer timer;

    public MeteredMessageHandler(MessageHandler<M> actualHandler, MetricRegistry metricRegistry) {
        this.actualHandler = actualHandler;

        String metricsName = actualHandler.getClass().getName() + ".handle";
        this.meterRetryable = metricRegistry.meter(metricsName + ".retryable");
        this.meterError = metricRegistry.meter(metricsName + ".error");
        this.timer = metricRegistry.timer(metricsName);
    }

    @Override
    public void handle(M message) throws Exception {
        try (Timer.Context timerContext = timer.time()) {
            actualHandler.handle(message);
        } catch (RetryableMessageHandlerException e) {
            meterRetryable.mark();
            throw e;
        } catch (Exception e) {
            meterError.mark();
            throw e;
        }
    }
}
