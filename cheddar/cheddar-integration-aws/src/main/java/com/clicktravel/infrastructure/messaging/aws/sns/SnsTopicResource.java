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

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.policy.Policy;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.SetTopicAttributesRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.clicktravel.infrastructure.messaging.aws.sqs.SqsQueueResource;

/**
 * Represents an actual AWS SNS topic that exists in the AWS environment. Provides some convenience methods for working
 * with the actual AWS SNS topic.
 */
public class SnsTopicResource {

    private static final String TOPIC_POLICY_ATTRIBUTE = "Policy";

    private final String topicName;
    private final String topicArn;
    private final AmazonSNS amazonSnsClient;

    public SnsTopicResource(final String topicName, final String topicArn, final AmazonSNS amazonSnsClient) {
        this.topicName = topicName;
        this.topicArn = topicArn;
        this.amazonSnsClient = amazonSnsClient;
    }

    /**
     * Publish a message with subject to the AWS SNS topic.
     * @param subject "Subject" line of message to publish
     * @param message Content of message to publish
     * @throws AmazonClientException
     */
    public void publish(final String subject, final String message) throws AmazonClientException {
        amazonSnsClient.publish(new PublishRequest().withTopicArn(topicArn).withSubject(subject).withMessage(message));
    }

    /**
     * Adds an AWS SQS subscription to the AWS SNS topic.
     * @param sqsQueueResource {@link SqsQueueResource} representative of AWS SQS queue subscribing to the AWS SNS
     *            topic.
     * @throws AmazonClientException
     */
    public void subscribe(final SqsQueueResource sqsQueueResource) throws AmazonClientException {
        amazonSnsClient.subscribe(new SubscribeRequest(topicArn, "sqs", sqsQueueResource.queueArn()));
    }

    /**
     * Sets the {@link Policy} of the AWS SNS topic
     * @param policy {@link Policy} to set
     * @throws AmazonClientException
     */
    public void setPolicy(final Policy policy) throws AmazonClientException {
        amazonSnsClient
                .setTopicAttributes(new SetTopicAttributesRequest(topicArn, TOPIC_POLICY_ATTRIBUTE, policy.toJson()));
    }

    /**
     * @return The AWS SNS topic name
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * @return The ARN of the AWS SNS topic
     */
    public String getTopicArn() {
        return topicArn;
    }

}
