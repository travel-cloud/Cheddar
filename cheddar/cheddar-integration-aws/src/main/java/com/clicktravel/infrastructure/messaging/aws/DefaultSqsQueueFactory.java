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
package com.clicktravel.infrastructure.messaging.aws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

@Component
public class DefaultSqsQueueFactory implements SqsQueueFactory {

    private static final String SQS_VISIBILITY_TIMEOUT_ATTRIBUTE = QueueAttributeName.VisibilityTimeout.toString();
    private static final String SQS_VISIBILITY_TIMEOUT_VALUE = "300";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AmazonSQS amazonSqsClient;

    @Autowired
    public DefaultSqsQueueFactory(final AmazonSQS amazonSqsClient) {
        this.amazonSqsClient = amazonSqsClient;
    }

    @Override
    public SqsQueue createSqsQueue(final String name, final SnsTopic... snsTopics) {
        String queueUrl;
        try {
            queueUrl = amazonSqsClient.getQueueUrl(new GetQueueUrlRequest(name)).getQueueUrl();
        } catch (final QueueDoesNotExistException e) {
            queueUrl = createAwsSqsQueue(name);
        }
        final SqsQueue sqsQueue = new SqsQueue(name, queueUrl, amazonSqsClient);

        // If this queue should subscribe to any topics, create (or update existing) subscriptions and queue policy
        if (snsTopics.length != 0) {
            sqsQueue.setPolicy(acceptMessagesFromTopicsPolicy(sqsQueue, snsTopics));
            for (final SnsTopic snsTopic : snsTopics) {
                snsTopic.subscribe(sqsQueue);
            }
        }

        return sqsQueue;
    }

    private String createAwsSqsQueue(final String name) {
        logger.debug("Creating SQS queue: " + name);
        final Map<String, String> attributes = new HashMap<>();
        attributes.put(SQS_VISIBILITY_TIMEOUT_ATTRIBUTE, SQS_VISIBILITY_TIMEOUT_VALUE);
        final CreateQueueRequest createQueueRequest = new CreateQueueRequest(name).withAttributes(attributes);
        return amazonSqsClient.createQueue(createQueueRequest).getQueueUrl();
    }

    private Policy acceptMessagesFromTopicsPolicy(final SqsQueue sqsQueue, final SnsTopic... snsTopics) {
        final Collection<Statement> statements = new ArrayList<>();
        for (final SnsTopic snsTopic : snsTopics) {
            statements.add(acceptMessagesFromTopicStatement(sqsQueue, snsTopic));
        }
        final Policy policy = new Policy();
        policy.setStatements(statements);
        return policy;
    }

    private Statement acceptMessagesFromTopicStatement(final SqsQueue sqsQueue, final SnsTopic snsTopic) {
        return new Statement(Effect.Allow)
                .withPrincipals(Principal.AllUsers)
                .withActions(SQSActions.SendMessage)
                .withResources(new Resource(sqsQueue.queueArn()))
                .withConditions(
                        new ArnCondition(ArnComparisonType.ArnEquals, ConditionFactory.SOURCE_ARN_CONDITION_KEY,
                                snsTopic.getTopicArn()));
    }
}
