package com.izettle.alb;

import static java.util.Objects.requireNonNull;

import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancingv2.model.DescribeTargetHealthRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.DescribeTargetHealthResult;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Precondition that check that enough instances are available in the ELB before the precondition is met.
 */
public class ALBAvailableInstancesCondition implements Condition {

    private final AmazonElasticLoadBalancingClient elbClient;
    private final String targetGroupArn;
    private final int minNumberOfAvailableInstances;
    private final int maxNumberOfAvailableInstances;
    private final long millisToSleepBetweenConditionCheck;
    private final String instanceId;

    public ALBAvailableInstancesCondition(
        AmazonElasticLoadBalancingClient elbClient,
        String targetGroupArn,
        String instanceId,
        int minNumberOfAvailableInstances,
        int maxNumberOfAvailableInstances,
        long millisToSleepBetweenConditionCheck
    ) {

        requireNonNull(elbClient);
        requireNonNull(targetGroupArn);
        requireNonNull(instanceId);

        this.instanceId = instanceId;
        this.millisToSleepBetweenConditionCheck = millisToSleepBetweenConditionCheck;
        this.minNumberOfAvailableInstances = minNumberOfAvailableInstances;
        this.maxNumberOfAvailableInstances = maxNumberOfAvailableInstances;

        this.targetGroupArn = targetGroupArn;
        this.elbClient = elbClient;
    }

    @Override
    public CompletableFuture<Boolean> check(
        ExecutorService executorService,
        Consumer<String> notificationsConsumer
    ) {

        return CompletableFuture.supplyAsync(
            () -> {
                while (true) {

                    // First await that there are enough instances available
                    DescribeTargetHealthResult describeTargetHealthResult =
                        elbClient.describeTargetHealth(new DescribeTargetHealthRequest().withTargetGroupArn(
                            targetGroupArn));

                    // Number of healthy instances other than ourselves
                    final long availableInstances = describeTargetHealthResult.getTargetHealthDescriptions()
                        .stream()
                        .filter(thd -> !thd.getTarget().getId().equals(instanceId))
                        .filter(thd -> thd.getTargetHealth().getState().equals("healthy"))
                        .count();

                    if (availableInstances >= minNumberOfAvailableInstances
                        && availableInstances <= maxNumberOfAvailableInstances) {
                        notificationsConsumer.accept(
                            String.format(
                                "ELB Precondition was met [avail: %s min: %s max: %s].",
                                availableInstances,
                                minNumberOfAvailableInstances,
                                maxNumberOfAvailableInstances
                            ));
                        return true;
                    }

                    notificationsConsumer.accept(
                        String.format(
                            "ELB Precondition was not met [avail: %s min: %s max: %s].",
                            availableInstances,
                            minNumberOfAvailableInstances,
                            maxNumberOfAvailableInstances
                        ));

                    try {
                        TimeUnit.MILLISECONDS.sleep(millisToSleepBetweenConditionCheck);
                    } catch (InterruptedException e) {
                        // Do nothing.
                        notificationsConsumer.accept("ELB Available Instances Preconditions was unable to sleep");
                    }
                }
            }, executorService);
    }
}
