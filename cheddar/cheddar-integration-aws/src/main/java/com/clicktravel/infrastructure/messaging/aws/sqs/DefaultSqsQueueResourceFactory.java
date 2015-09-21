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
package com.clicktravel.infrastructure.messaging.aws.sqs;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.amazonaws.auth.policy.*;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.auth.policy.actions.SQSActions;
import com.amazonaws.auth.policy.conditions.ArnCondition;
import com.amazonaws.auth.policy.conditions.ArnCondition.ArnComparisonType;
import com.amazonaws.auth.policy.conditions.ConditionFactory;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.clicktravel.common.functional.StringUtils;
import com.clicktravel.infrastructure.messaging.aws.sns.SnsTopicResource;

@Component
public class DefaultSqsQueueResourceFactory implements SqsQueueResourceFactory {

    private static final String SQS_VISIBILITY_TIMEOUT_ATTRIBUTE = QueueAttributeName.VisibilityTimeout.toString();
    private static final String SQS_VISIBILITY_TIMEOUT_VALUE = "300";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AmazonSQS amazonSqsClient;

    @Autowired
    public DefaultSqsQueueResourceFactory(final AmazonSQS amazonSqsClient) {
        this.amazonSqsClient = amazonSqsClient;
    }

    @Override
    public SqsQueueResource createSqsQueueResource(final String name, final SnsTopicResource... snsTopics) {
        final String queueUrl = amazonSqsClient.getQueueUrl(new GetQueueUrlRequest(name)).getQueueUrl();
        logger.info("Using existing SQS queue: " + name);
        final SqsQueueResource sqsQueueResource = new SqsQueueResource(name, queueUrl, amazonSqsClient);
        return sqsQueueResource;

    }

    @Override
    public SqsQueueResource createSqsQueueResourceAndAwsSqsQueueIfAbsent(final String name,
            final SnsTopicResource... snsTopics) {
        try {
            final SqsQueueResource sqsQueueResource = createSqsQueueResource(name, snsTopics);
            return sqsQueueResource;
        } catch (final QueueDoesNotExistException e) {
            final SqsQueueResource sqsQueueResource = new SqsQueueResource(name, createAwsSqsQueue(name),
                    amazonSqsClient);
            subscribeToSnsTopics(name, sqsQueueResource, snsTopics);
            return sqsQueueResource;
        }
    }

    private String createAwsSqsQueue(final String name) {
        logger.info("Creating SQS queue: " + name);
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(SQS_VISIBILITY_TIMEOUT_ATTRIBUTE, SQS_VISIBILITY_TIMEOUT_VALUE);
        final CreateQueueRequest createQueueRequest = new CreateQueueRequest(name).withAttributes(attributes);
        return amazonSqsClient.createQueue(createQueueRequest).getQueueUrl();
    }

    private void subscribeToSnsTopics(final String name, final SqsQueueResource sqsQueueResource,
            final SnsTopicResource... snsTopics) {
        // If this queue should subscribe to any topics, create (or update existing) subscriptions and queue policy
        if (snsTopics.length != 0) {
            logger.info("Adding SQS queue [" + name + "] as a subscriber for these SNS topics: ["
                    + snsTopicNames(snsTopics) + "]");
            sqsQueueResource.setPolicy(acceptMessagesFromTopicsPolicy(sqsQueueResource, snsTopics));
            for (final SnsTopicResource snsTopicResource : snsTopics) {
                snsTopicResource.subscribe(sqsQueueResource);
            }
        }
    }

    private Policy acceptMessagesFromTopicsPolicy(final SqsQueueResource sqsQueueResource,
            final SnsTopicResource... snsTopics) {
        final Collection<Statement> statements = new ArrayList<>();
        for (final SnsTopicResource snsTopicResource : snsTopics) {
            statements.add(acceptMessagesFromTopicStatement(sqsQueueResource, snsTopicResource));
        }
        final Policy policy = new Policy();
        policy.setStatements(statements);
        return policy;
    }

    private Statement acceptMessagesFromTopicStatement(final SqsQueueResource sqsQueueResource,
            final SnsTopicResource snsTopicResource) {
        return new Statement(Effect.Allow)
                .withPrincipals(Principal.AllUsers)
                .withActions(SQSActions.SendMessage)
                .withResources(new Resource(sqsQueueResource.queueArn()))
                .withConditions(
                        new ArnCondition(ArnComparisonType.ArnEquals, ConditionFactory.SOURCE_ARN_CONDITION_KEY,
                                snsTopicResource.getTopicArn()));
    }

    private String snsTopicNames(final SnsTopicResource[] snsTopics) {
        final List<String> names = new ArrayList<>();
        for (final SnsTopicResource snsTopic : snsTopics) {
            names.add(snsTopic.getTopicName());
        }
        return StringUtils.join(names);
    }

}
