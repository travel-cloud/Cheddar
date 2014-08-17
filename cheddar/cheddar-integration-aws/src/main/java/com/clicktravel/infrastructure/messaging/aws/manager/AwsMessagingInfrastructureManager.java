/*
 * Copyright 2014 Click Travel Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.clicktravel.infrastructure.messaging.aws.manager;

import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.policy.*;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.auth.policy.actions.SNSActions;
import com.amazonaws.auth.policy.actions.SQSActions;
import com.amazonaws.auth.policy.conditions.ArnCondition;
import com.amazonaws.auth.policy.conditions.ArnCondition.ArnComparisonType;
import com.amazonaws.auth.policy.conditions.ConditionFactory;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.*;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.clicktravel.cheddar.infrastructure.messaging.ExchangeQueueBinding;
import com.clicktravel.infrastructure.messaging.aws.SnsMessagePublisher;
import com.clicktravel.infrastructure.messaging.aws.SqsMessageQueueAccessor;

/**
 * Amazon Web Services Messaging Infrastructure Manager
 * 
 * The responsibility of implementing classes is to create queues and topics, fetch queue and topic-related information
 * @author bjanjua
 * 
 */
public class AwsMessagingInfrastructureManager {

    private static final String AWS_POLICY_ATTRIBUTE = "Policy";
    private static final String SQS_QUEUE_ARN_ATTRIBUTE = "QueueArn";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AmazonSQS amazonSqsClient;
    private final AmazonSNS amazonSnsClient;
    private final Collection<SqsMessageQueueAccessor> messageQueueAccessors;
    private final Collection<SnsMessagePublisher> messagePublishers;
    private final Collection<ExchangeQueueBinding> exchangeQueueBindings;

    public AwsMessagingInfrastructureManager(final AmazonSQS amazonSqsClient, final AmazonSNS amazonSnsClient,
            final Collection<SqsMessageQueueAccessor> messageQueueAccessors) {
        this.amazonSqsClient = amazonSqsClient;
        this.amazonSnsClient = amazonSnsClient;
        this.messageQueueAccessors = new HashSet<>();
        if (messageQueueAccessors != null) {
            this.messageQueueAccessors.addAll(messageQueueAccessors);
        }
        messagePublishers = new HashSet<>();
        exchangeQueueBindings = new HashSet<>();
    }

    public void setMessagePublishers(final Collection<SnsMessagePublisher> messagePublishers) {
        if (this.messagePublishers != null) {
            this.messagePublishers.addAll(messagePublishers);
        }
    }

    public void setExchangeQueueBindings(final Collection<ExchangeQueueBinding> exchangeQueueBindings) {
        if (this.exchangeQueueBindings != null) {
            this.exchangeQueueBindings.addAll(exchangeQueueBindings);
        }
    }

    public void init() {
        logger.debug("Validating queues");
        for (final SqsMessageQueueAccessor messageQueueAccessor : messageQueueAccessors) {
            final String queueName = messageQueueAccessor.queueName();
            logger.debug("Checking if queue exists: " + queueName);
            if (queueExists(queueName)) {
                logger.debug("Queue already exists: " + queueName);
            } else {
                createQueue(queueName);
            }
            messageQueueAccessor.configure(amazonSqsClient);
        }
        logger.debug("Validating exchanges");
        for (final SnsMessagePublisher messagePublisher : messagePublishers) {
            final String exchangeName = messagePublisher.exchangeName();
            logger.debug("Checking if exchange exists: " + exchangeName);
            String topicArn = topicArnForExchangeName(exchangeName);
            if (topicArn != null) {
                logger.debug("Exchange already exists: " + exchangeName);
            } else {
                topicArn = createExchange(exchangeName);
            }
            messagePublisher.configure(amazonSnsClient, topicArn);
        }
        logger.debug("Validating exchange-queue bindings");
        final Map<String, Set<String>> exchangeQueueNameMap = new HashMap<>();
        final Set<String> missingExchangeNames = new HashSet<>();
        for (final ExchangeQueueBinding exchangeQueueBinding : exchangeQueueBindings) {
            final String queueName = exchangeQueueBinding.queueName();
            Set<String> exchangeNames = exchangeQueueNameMap.get(queueName);
            if (exchangeNames == null) {
                exchangeNames = new HashSet<String>();
            }
            final String exchangeName = exchangeQueueBinding.exchangeName();
            if (topicArnForExchangeName(exchangeName) != null) {
                exchangeNames.add(exchangeName);
            } else {
                logger.debug("Waiting for response from exchange: " + exchangeName);
                for (int i = 0; i < 300; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (final InterruptedException ie) {
                        throw new IllegalStateException(ie);
                    }
                    if (topicArnForExchangeName(exchangeName) != null) {
                        exchangeNames.add(exchangeName);
                        logger.debug("Exchange responded: " + exchangeName);
                        break;
                    }
                }
                if (!exchangeNames.contains(exchangeName)) {
                    logger.debug("Exchange unresponsive: " + exchangeName);
                    missingExchangeNames.add(exchangeName);
                }
            }
            exchangeQueueNameMap.put(queueName, exchangeNames);
        }
        for (final Entry<String, Set<String>> entrySet : exchangeQueueNameMap.entrySet()) {
            createExchangeQueueBinding(entrySet.getKey(), entrySet.getValue());
        }
        if (missingExchangeNames.size() > 0) {
            throw new IllegalStateException("Could not create bindings for missing exchanges: " + missingExchangeNames);
        }
    }

    private void createExchangeQueueBinding(final String queueName, final Collection<String> exchangeNames) {
        final String queueUrl = queueUrlForQueueName(queueName);
        final String queueArn = queueArnForQueueUrl(queueUrl);
        final Policy policy = new Policy();
        final Collection<Statement> statements = new ArrayList<>();
        for (final String exchangeName : exchangeNames) {
            final String topicArn = topicArnForExchangeName(exchangeName);
            amazonSnsClient.subscribe(new SubscribeRequest(topicArn, "sqs", queueArn));
            statements.add(exchangeBindingPolicyStatement(topicArn, queueArn));
            logger.debug("Binding created: " + exchangeName + " exchange -> " + queueName + " queue");
        }
        policy.setStatements(statements);
        final Map<String, String> queueAttributes = new HashMap<>();
        queueAttributes.put(AWS_POLICY_ATTRIBUTE, policy.toJson());
        amazonSqsClient.setQueueAttributes(new SetQueueAttributesRequest(queueUrl, queueAttributes));

    }

    private Statement exchangeBindingPolicyStatement(final String topicArn, final String queueArn) {
        final Statement statement = new Statement(Effect.Allow)
                .withPrincipals(Principal.AllUsers)
                .withActions(SQSActions.SendMessage)
                .withResources(new Resource(queueArn))
                .withConditions(
                        new ArnCondition(ArnComparisonType.ArnEquals, ConditionFactory.SOURCE_ARN_CONDITION_KEY,
                                topicArn));
        return statement;
    }

    private String queueUrlForQueueName(final String queueName) {
        return amazonSqsClient.getQueueUrl(new GetQueueUrlRequest(queueName)).getQueueUrl();
    }

    private String queueArnForQueueUrl(final String queueUrl) {
        final GetQueueAttributesResult getQueueAttributesResult = amazonSqsClient
                .getQueueAttributes(new GetQueueAttributesRequest(queueUrl).withAttributeNames(SQS_QUEUE_ARN_ATTRIBUTE));
        final String queueArn = getQueueAttributesResult.getAttributes().get(SQS_QUEUE_ARN_ATTRIBUTE);
        return queueArn;
    }

    /**
     * Concrete classes should provide a method of finding out if a queue with the given name exists
     * @param queueName
     * @return Does the queue with the given name exists
     */
    public boolean queueExists(final String queueName) {
        final GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest(queueName);
        try {
            amazonSqsClient.getQueueUrl(getQueueUrlRequest);
            return true;
        } catch (final QueueDoesNotExistException e) {
            return false;
        }
    }

    public void createQueue(final String queueName) {
        logger.debug("Creating queue: " + queueName);
        final CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
        amazonSqsClient.createQueue(createQueueRequest);
    }

    public String createExchange(final String exchangeName) {
        logger.debug("Creating exchange: " + exchangeName);
        final CreateTopicRequest createTopicRequest = new CreateTopicRequest(exchangeName);
        final CreateTopicResult result = amazonSnsClient.createTopic(createTopicRequest);
        final String topicArn = result.getTopicArn();
        final String[] topicArnParts = topicArn.split(":");
        final String sourceOwner = topicArnParts[topicArnParts.length - 2];
        final Condition condition = new Condition().withType("StringEquals").withConditionKey("AWS:SourceOwner")
                .withValues(sourceOwner);
        final Action receiveAction = new Action() {

            @Override
            public String getActionName() {
                return "sns:Receive";
            }
        };
        final Statement recieveStatement = new Statement(Effect.Allow).withPrincipals(Principal.AllUsers)
                .withActions(receiveAction).withResources(new Resource(topicArn)).withConditions(condition);
        final Statement subscribeStatement = new Statement(Effect.Allow).withPrincipals(Principal.AllUsers)
                .withActions(SNSActions.Subscribe);
        final Policy snsPolicy = new Policy().withStatements(recieveStatement, subscribeStatement);
        amazonSnsClient.setTopicAttributes(new SetTopicAttributesRequest(topicArn, AWS_POLICY_ATTRIBUTE, snsPolicy
                .toJson()));
        return topicArn;
    }

    /**
     * Checks to see if the exchange exists in AWS.
     * 
     * @param exchangeName
     * @return
     */
    public String topicArnForExchangeName(final String exchangeName) {
        String nextToken = null;
        String topicArn = null;
        while (topicArn == null) {
            final ListTopicsResult listTopicsResult = amazonSnsClient.listTopics(nextToken);
            topicArn = topicArnForExchangeNameInTopics(exchangeName, listTopicsResult.getTopics());
            nextToken = listTopicsResult.getNextToken();
            if (nextToken == null) {
                break;
            }
        }
        return topicArn;
    }

    private String topicArnForExchangeNameInTopics(final String exchangeName, final List<Topic> topics) {
        final String topicArnSuffix = ":" + exchangeName;
        for (final Topic topic : topics) {
            final String topicArn = topic.getTopicArn();
            final int pos = topicArn.lastIndexOf(topicArnSuffix);
            if (pos == -1) {
                continue;
            }
            final String actualExchangeName = topicArn.substring(pos + 1);
            if (actualExchangeName.equals(exchangeName)) {
                return topicArn;
            }
        }
        return null;
    }

}
