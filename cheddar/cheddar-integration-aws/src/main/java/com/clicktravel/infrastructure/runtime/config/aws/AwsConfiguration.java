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
package com.clicktravel.infrastructure.runtime.config.aws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.clicktravel.common.http.client.HttpClient;
import com.clicktravel.infrastructure.host.aws.ec2.Ec2InstanceData;
import com.clicktravel.infrastructure.host.aws.ec2.Ec2InstanceDataAccessor;
import com.clicktravel.infrastructure.persistence.aws.cloudsearch.client.CloudSearchClient;

@Configuration
public class AwsConfiguration {

    private static final String INSTANCE_BASE_URI = "http://169.254.169.254/latest";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Bean
    @Autowired
    public AmazonSQS amazonSqsClient(final AWSCredentials awsCredentials,
            @Value("${aws.sqs.client.endpoint}") final String endpoint) {
        final AmazonSQS amazonSqsClient = new AmazonSQSClient(awsCredentials);
        logger.info("Setting AWS SQS endpoint to: " + endpoint);
        amazonSqsClient.setEndpoint(endpoint);
        return amazonSqsClient;
    }

    @Bean
    @Autowired
    public AmazonSNS amazonSnsClient(final AWSCredentials awsCredentials,
            @Value("${aws.sns.client.endpoint}") final String endpoint) {
        final AmazonSNS amazonSnsClient = new AmazonSNSClient(awsCredentials);
        logger.info("Setting AWS SNS endpoint to: " + endpoint);
        amazonSnsClient.setEndpoint(endpoint);
        return amazonSnsClient;
    }

    @Bean
    @Autowired
    public AmazonDynamoDB amazonDynamoDbClient(final AWSCredentials awsCredentials,
            @Value("${aws.dynamodb.client.endpoint}") final String endpoint) {
        final AmazonDynamoDB amazonDynamoDbClient = new AmazonDynamoDBClient(awsCredentials);
        logger.info("Setting AWS DynamoDB endpoint to: " + endpoint);
        amazonDynamoDbClient.setEndpoint(endpoint);
        return amazonDynamoDbClient;
    }

    @Bean
    @Autowired
    public AmazonS3 amazonS3Client(final AWSCredentials awsCredentials,
            @Value("${aws.s3.client.endpoint}") final String endpoint) {
        final AmazonS3 amazonS3Client = new AmazonS3Client(awsCredentials);
        logger.info("Setting AWS S3 endpoint to: " + endpoint);
        amazonS3Client.setEndpoint(endpoint);
        return amazonS3Client;
    }

    @Bean
    @Autowired
    public CloudSearchClient cloudSearchClient(final AWSCredentials awsCredentials,
            @Value("${aws.cloudsearch.client.endpoint}") final String endpoint) {
        final CloudSearchClient cloudSearchClient = new CloudSearchClient(awsCredentials);
        logger.info("Setting AWS CloudSearch endpoint to: " + endpoint);
        cloudSearchClient.setEndpoint(endpoint);
        cloudSearchClient.initialize();
        return cloudSearchClient;
    }

    @Bean
    @Autowired
    public Ec2InstanceData localhostEc2InstanceData() {
        final HttpClient client = HttpClient.Builder.httpClient().withBaseUri(INSTANCE_BASE_URI).build();
        return new Ec2InstanceDataAccessor(client);
    }
}
