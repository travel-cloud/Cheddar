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
package com.clicktravel.infrastructure.tx.aws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.clicktravel.cheddar.infrastructure.tx.TransactionalResourceManager;
import com.clicktravel.infrastructure.messaging.aws.tx.TransactionalSnsMessagePublisher;
import com.clicktravel.infrastructure.messaging.aws.tx.TransactionalSqsMessageSender;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.tx.TransactionalDynamoDbTemplate;
import com.clicktravel.infrastructure.persistence.aws.s3.tx.TransactionalS3FileStore;

@Configuration
public class AwsTransactionalResourceManagerConfiguration {

    @Autowired(required = false)
    private TransactionalDynamoDbTemplate transactionalDynamoDbTemplate;
    @Autowired(required = false)
    private TransactionalS3FileStore transactionalS3FileStore;
    @Autowired(required = false)
    private TransactionalSqsMessageSender transactionalSqsMessageSender;
    @Autowired(required = false)
    private TransactionalSnsMessagePublisher transactionalSnsMessagePublisher;

    @Bean
    @Autowired
    public TransactionalResourceManager transactionalResourceManager() {
        final TransactionalResourceManager transactionalResourceManager = new AwsTransactionalResourceManager(
                transactionalDynamoDbTemplate, transactionalS3FileStore, transactionalSqsMessageSender,
                transactionalSnsMessagePublisher);
        return transactionalResourceManager;
    }

}
