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
package com.clicktravel.infrastructure.integration.aws;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AwsIntegration {

    private static final AwsIntegration INSTANCE = new AwsIntegration();
    private final String accountId;
    private final String accessKeyId;
    private final String secretKeyId;
    private final String dynamoDbEndpoint;
    private final String cloudSearchEndpoint;

    public AwsIntegration() {
        final Properties props = new Properties();
        final InputStream fileIn = Class.class
                .getResourceAsStream("/com.clicktravel.infrastructure.integration.aws.properties");
        try {
            props.load(fileIn);
        } catch (final IOException e) {
            throw new IllegalStateException("Missing properties file");
        }
        accountId = props.getProperty("aws.account.id");
        accessKeyId = props.getProperty("aws.access.key.id");
        secretKeyId = props.getProperty("aws.secret.key.id");
        dynamoDbEndpoint = props.getProperty("aws.dynamodb.endpoint");
        cloudSearchEndpoint = props.getProperty("aws.cloudsearch.endpoint");
    }

    public static String getAccountId() {
        return INSTANCE.accountId;
    }

    public static String getAccessKeyId() {
        return INSTANCE.accessKeyId;
    }

    public static String getSecretKeyId() {
        return INSTANCE.secretKeyId;
    }

    public static String getDynamoDbEndpoint() {
        return INSTANCE.dynamoDbEndpoint;
    }

    public static String getCloudSearchEndpoint() {
        return INSTANCE.cloudSearchEndpoint;
    }

}
