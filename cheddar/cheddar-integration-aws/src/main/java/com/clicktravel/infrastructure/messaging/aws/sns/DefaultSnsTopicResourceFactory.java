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
package com.clicktravel.infrastructure.messaging.aws.sns;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.auth.policy.*;
import com.amazonaws.auth.policy.Statement.Effect;
import com.amazonaws.auth.policy.actions.SNSActions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.Topic;

public class DefaultSnsTopicResourceFactory implements SnsTopicResourceFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final AmazonSNS amazonSnsClient;

    @Autowired
    public DefaultSnsTopicResourceFactory(final AmazonSNS amazonSnsClient) {
        this.amazonSnsClient = amazonSnsClient;
    }

    @Override
    public SnsTopicResource createSnsTopicResource(final String name) {
        final String topicArn = pollAndRetryForTopicArnForName(name);
        logger.info("Using existing SNS topic: " + name);
        return new SnsTopicResource(name, topicArn, amazonSnsClient);
    }

    @Override
    public SnsTopicResource createSnsTopicResourceAndAwsSnsTopicIfAbsent(final String name) {
        String topicArn = pollForTopicArnForName(name);
        if (topicArn == null) {
            topicArn = createAwsSnsTopic(name);
            final SnsTopicResource snsTopicResource = new SnsTopicResource(name, topicArn, amazonSnsClient);
            // policy is set once, on creation of SNS topic
            snsTopicResource.setPolicy(allowAllQueuesPolicy(snsTopicResource));
            return snsTopicResource;
        } else {
            logger.info("Using existing SNS topic: " + name);
            return new SnsTopicResource(name, topicArn, amazonSnsClient);
        }
    }

    private Policy allowAllQueuesPolicy(final SnsTopicResource snsTopicResource) {
        final String topicArn = snsTopicResource.getTopicArn();
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
        return new Policy().withStatements(recieveStatement, subscribeStatement);
    }

    private String createAwsSnsTopic(final String name) {
        logger.info("Creating SNS topic: " + name);
        return amazonSnsClient.createTopic(new CreateTopicRequest(name)).getTopicArn();
    }

    private String pollAndRetryForTopicArnForName(final String name) {
        for (int i = 0; i < 300; i++) {
            final String topicArn = pollForTopicArnForName(name);
            if (topicArn != null) {
                return topicArn;
            }
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        logger.error("SNS topic is unresponsive: " + name);
        throw new IllegalStateException("Could not create subscriptions for missing SNS topic: " + name);
    }

    private String pollForTopicArnForName(final String name) {
        String nextToken = null;
        String topicArn = null;
        while (topicArn == null) {
            final ListTopicsResult listTopicsResult = amazonSnsClient.listTopics(nextToken);
            topicArn = topicArnForNameInTopics(name, listTopicsResult.getTopics());
            nextToken = listTopicsResult.getNextToken();
            if (nextToken == null) {
                break;
            }
        }
        return topicArn;
    }

    private String topicArnForNameInTopics(final String name, final List<Topic> topics) {
        final String topicArnSuffix = ":" + name;
        for (final Topic topic : topics) {
            final String topicArn = topic.getTopicArn();
            final int pos = topicArn.lastIndexOf(topicArnSuffix);
            if (pos == -1) {
                continue;
            }
            final String actualName = topicArn.substring(pos + 1);
            if (actualName.equals(name)) {
                return topicArn;
            }
        }
        return null;
    }
}
