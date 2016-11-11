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

import com.amazonaws.services.sns.AmazonSNS;
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
        logger.info("Looking-up ARN for SNS topic: " + name);
        final String topicArn = topicArnForName(name);
        logger.info("Using existing SNS topic: " + name);
        return new SnsTopicResource(name, topicArn, amazonSnsClient);
    }

    private String topicArnForName(final String name) {
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
