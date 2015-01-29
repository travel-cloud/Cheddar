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

import java.util.Properties;

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.simpleemail.AWSJavaMailTransport;

public class AmazonMailSender extends JavaMailSenderImpl {

    private static final String MAIL_TRANSPORT_PROTOCOL_KEY = "mail.transport.protocol";
    private final AWSCredentials awsCredentials;

    public AmazonMailSender(final AWSCredentials awsCredentials) {
        this.awsCredentials = awsCredentials;
    }

    public void init() {
        final Properties props = getJavaMailProperties();
        props.setProperty(MAIL_TRANSPORT_PROTOCOL_KEY, "aws");
        props.setProperty(AWSJavaMailTransport.AWS_ACCESS_KEY_PROPERTY, awsCredentials.getAWSAccessKeyId());
        props.setProperty(AWSJavaMailTransport.AWS_SECRET_KEY_PROPERTY, awsCredentials.getAWSSecretKey());
        // set port to -1 to ensure that Spring calls the equivalent of "transport.connect()"
        setPort(-1);
    }

    @Override
    public Transport getTransport(final Session session) throws NoSuchProviderException {
        return new AWSJavaMailTransport(session, null);
    }

}