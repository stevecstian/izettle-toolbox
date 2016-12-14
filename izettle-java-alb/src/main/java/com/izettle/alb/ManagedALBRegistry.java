package com.izettle.alb;

import static java.util.Objects.requireNonNull;

import com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancingv2.model.DeregisterTargetsRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.DeregisterTargetsResult;
import com.amazonaws.services.elasticloadbalancingv2.model.DescribeTargetGroupAttributesRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.DescribeTargetGroupAttributesResult;
import com.amazonaws.services.elasticloadbalancingv2.model.RegisterTargetsRequest;
import com.amazonaws.services.elasticloadbalancingv2.model.RegisterTargetsResult;
import com.amazonaws.services.elasticloadbalancingv2.model.TargetDescription;
import com.amazonaws.services.elasticloadbalancingv2.model.TargetGroupAttribute;
import io.dropwizard.lifecycle.Managed;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registry has functions for registering an application under a target group for discovery
 * as well as
 * <p/>
 * In order for the target group to work the AWS user needs these types of permissions
 * <p/>
 {
 "Version": "2012-10-17",
 "Statement": [
 {
 "Effect": "Allow",
 "Action": [
 "elasticloadbalancing:RegisterTargets",
 "elasticloadbalancing:DeregisterTargets",
 "elasticloadbalancing:DescribeTargetHealth",
 "elasticloadbalancing:DescribeTargetGroupAttributes"
 ],
 "Resource": "*"
 }
 ]
 }
 * <p/>
 */
public class ManagedALBRegistry implements Managed {

    private static final Logger LOG = LoggerFactory.getLogger(ManagedALBRegistry.class.getName());

    private final AmazonElasticLoadBalancingClient elbClient;
    private final String targetGroupArn;
    private final Condition deregisterCondition;
    private final String instanceId;

    /**
     * Create ALB Registry
     *
     * @param elbClient                    The ELB AWS Client
     * @param targetGroupArn               The ARN of the target group
     * @param instanceId                   The instance ID of the host where the App is running.
     * @param deregisterCondition          Precondition before deregister can be executed.
     */
    public ManagedALBRegistry(
        AmazonElasticLoadBalancingClient elbClient,
        String targetGroupArn,
        String instanceId,
        Condition deregisterCondition
    ) {

        requireNonNull(elbClient);
        requireNonNull(targetGroupArn);
        requireNonNull(instanceId);
        requireNonNull(deregisterCondition);

        this.instanceId = instanceId;
        this.deregisterCondition = deregisterCondition;
        this.targetGroupArn = targetGroupArn;
        this.elbClient = elbClient;
    }

    /**
     * Register the application in the target group
     */
    @Override
    public void start() throws Exception {
        LOG.info("Registering instance ID {} in target group {}", instanceId, targetGroupArn);

        final RegisterTargetsRequest registerTargetsRequest = new RegisterTargetsRequest()
            .withTargetGroupArn(targetGroupArn)
            .withTargets(new TargetDescription().withId(instanceId));

        //TODO: No way to check response?
        RegisterTargetsResult registerTargetsResult = elbClient.registerTargets(registerTargetsRequest);

    }

    /**
     * Deregister the application in the target group
     */
    @Override
    public void stop() throws Exception {
        LOG.info("Deregistering instance ID {} in target group {}", instanceId, targetGroupArn);
        // Make sure precondition is met before moving on.
        final CompletableFuture<Boolean> await = deregisterCondition.check(
            Executors::newSingleThreadExecutor,
            LOG::info
        );

        // Request connection draining settings to check if we should wait after deregister to stop the
        // instance.
        DescribeTargetGroupAttributesRequest describeTargetGroupAttributesRequest =
            new DescribeTargetGroupAttributesRequest().withTargetGroupArn(targetGroupArn);

        DescribeTargetGroupAttributesResult describeTargetGroupAttributesResult =
            elbClient.describeTargetGroupAttributes(describeTargetGroupAttributesRequest);

        Optional<String> drainingTime = describeTargetGroupAttributesResult.getAttributes().stream()
            .filter(a -> a.getKey().equals("deregistration_delay.timeout_seconds"))
            .map(TargetGroupAttribute::getValue)
            .findAny();

        await.get();

        // De-register
        DeregisterTargetsRequest deregisterTargetsRequest =
            new DeregisterTargetsRequest().withTargetGroupArn(targetGroupArn)
                .withTargets(new TargetDescription().withId(instanceId));

        //TODO: Verify response?
        DeregisterTargetsResult deregisterTargetsResult =
            elbClient.deregisterTargets(deregisterTargetsRequest);

        if (drainingTime.isPresent()) {
            // Now block until the connection draining timeout has hit. This is to make sure no more
            // requests are coming into the instance from the ELB from connections that the ELB might have held with
            // clients.
            LOG.info(
                "Connection Draining {} Seconds before stopping instance after deregister instance {} in target group {}",
                drainingTime.get(),
                instanceId,
                targetGroupArn
            );

            Thread.sleep(TimeUnit.SECONDS.toMillis(Integer.parseInt(drainingTime.get())));
        }
    }
}
