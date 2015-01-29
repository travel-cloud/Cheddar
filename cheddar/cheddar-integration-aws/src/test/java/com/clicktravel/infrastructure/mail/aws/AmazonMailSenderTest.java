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
package com.clicktravel.infrastructure.mail.aws;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Properties;

import javax.mail.Session;
import javax.mail.Transport;

import org.junit.Test;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AWSJavaMailTransport;

public class AmazonMailSenderTest {

    @Test
    public void shouldInitializeWithCredentialsAndTransport_givenAwsCredentials() {
        // Given
        final String awsAccessKeyId = randomString(10);
        final String awsAccessSecretId = randomString(10);
        final AWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKeyId, awsAccessSecretId);
        final AmazonMailSender mailSender = new AmazonMailSender(awsCredentials);

        // When
        mailSender.init();
        final Properties properties = mailSender.getJavaMailProperties();

        // Then
        assertEquals("aws", properties.getProperty("mail.transport.protocol"));
        assertEquals(awsAccessKeyId, properties.getProperty(AWSJavaMailTransport.AWS_ACCESS_KEY_PROPERTY));
        assertEquals(awsAccessSecretId, properties.getProperty(AWSJavaMailTransport.AWS_SECRET_KEY_PROPERTY));
    }

    @Test
    public void shouldGetTransport_withSession() throws Exception {
        // Given
        final String awsAccessKeyId = randomString(10);
        final String awsAccessSecretId = randomString(10);
        final AWSCredentials awsCredentials = new BasicAWSCredentials(awsAccessKeyId, awsAccessSecretId);
        final AmazonMailSender mailSender = new AmazonMailSender(awsCredentials);
        final Session mockSession = Session.getDefaultInstance(mock(Properties.class));

        // When
        final Transport transport = mailSender.getTransport(mockSession);

        // Then
        assertNotNull(transport);
        assertTrue(transport instanceof AWSJavaMailTransport);
    }

}