package com.izettle.messaging;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.auth.policy.Principal;
import com.amazonaws.auth.policy.Resource;
import com.amazonaws.auth.policy.Statement;
import com.amazonaws.auth.policy.actions.SQSActions;
import com.amazonaws.auth.policy.conditions.ConditionFactory;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicRequest;
import com.amazonaws.services.sns.model.ListSubscriptionsByTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.SetQueueAttributesRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AmazonSNSSubscriptionSetup {

    /**
     * Sets up a queue so that it subscribes to all messages that gets published on the
     * specified topic. The queue and topic must be created before-hand, or an exception
     * will be thrown.
     *
     * This method will also configure the queue so that it has permissions to receive
     * messages from the topic.
     *
     * @param queueURL The queue that should receive the messages posted to the topic.
     * @param topicARN The topic whose messages should be posted to the queue.
     * @param amazonSQS Amazon SQS client.
     * @param amazonSNS Amazon SNS client.
     */
    public static void subscribeSQSQueueToSNSTopic(
        String queueURL,
        String topicARN,
        AmazonSQS amazonSQS,
        AmazonSNS amazonSNS
    ) {
        // Verify that the queue exists, and get its ARN
        String queueARN = getSQSQueueARN(amazonSQS, queueURL);

        // The "is already subscribing?"-check has a dual purpose: it will also verify that the
        // topic is already created. If the topic is not created beforehand, this will throw a
        // com.amazonaws.services.sns.model.NotFoundException
        if (isSQSQueueSubscribingToSNSTopic(amazonSNS, queueARN, topicARN)) {
            // Subscription already configured. Do nothing.
            return;
        }

        allowSQSQueueToReceiveMessagesFromSNSTopic(amazonSQS, queueURL, queueARN, topicARN);
        subscribeSQSQueueToSNSTopic(amazonSNS, queueARN, topicARN);
    }

    private static String getSQSQueueARN(AmazonSQS amazonSQS, String queueURL) {
        // This statement will throw if the queue does not exist.
        GetQueueAttributesResult queueAttributes = amazonSQS.getQueueAttributes(
            new GetQueueAttributesRequest()
                .withQueueUrl(queueURL)
                .withAttributeNames(QueueAttributeName.QueueArn)
        );
        return queueAttributes
            .getAttributes()
            .get(QueueAttributeName.QueueArn.name());
    }

    private static boolean isSQSQueueSubscribingToSNSTopic(
        AmazonSNS amazonSNS,
        String queueARN,
        String topicARN
    ) {
        // This statement will throw if the topic does not exist.
        ListSubscriptionsByTopicResult subscriptions = amazonSNS.listSubscriptionsByTopic(
            new ListSubscriptionsByTopicRequest()
                .withTopicArn(topicARN)
        );
        for (Subscription subscription : subscriptions.getSubscriptions()) {
            if (subscription.getEndpoint().equals(queueARN)) {
                return true;
            }
        }
        return false;
    }

    private static void allowSQSQueueToReceiveMessagesFromSNSTopic(
        AmazonSQS amazonSQS,
        String queueURL,
        String queueARN,
        String topicARN
    ) {
        GetQueueAttributesResult queueAttributesResult =
            amazonSQS.getQueueAttributes(
                new GetQueueAttributesRequest().withQueueUrl(queueURL).withAttributeNames(
                    QueueAttributeName.Policy
                )
            );

        String policyJson = queueAttributesResult.getAttributes().get(QueueAttributeName.Policy.name());

        final List<Statement> statements;
        if (policyJson != null) {
            statements = new ArrayList<>(Policy.fromJson(policyJson).getStatements());
        } else {
            // no policies yet exist
            statements = new ArrayList<>();
        }

        statements.add(
            new Statement(Statement.Effect.Allow)
                .withPrincipals(Principal.AllUsers)
                .withResources(new Resource(queueARN))
                .withActions(SQSActions.SendMessage)
                .withConditions(ConditionFactory.newSourceArnCondition(topicARN))
        );

        Policy policy = new Policy();
        policy.setStatements(statements);
        Map<String, String> queueAttributes = new HashMap<>();
        queueAttributes.put(QueueAttributeName.Policy.name(), policy.toJson());

        // Note that if the queue already has this policy, this will do nothing.
        amazonSQS.setQueueAttributes(
            new SetQueueAttributesRequest()
                .withQueueUrl(queueURL)
                .withAttributes(queueAttributes)
        );
    }

    private static void subscribeSQSQueueToSNSTopic(
        AmazonSNS amazonSNS,
        String queueARN,
        String topicARN
    ) {
        // Note that if there is already a subscription with these parameters, this will do nothing.
        amazonSNS.subscribe(
            new SubscribeRequest()
                .withTopicArn(topicARN)
                .withProtocol("sqs")
                .withEndpoint(queueARN)
        );
    }
}
