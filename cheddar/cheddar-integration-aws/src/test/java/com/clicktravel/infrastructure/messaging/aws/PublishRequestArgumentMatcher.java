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

import org.mockito.ArgumentMatcher;

import com.amazonaws.services.sns.model.PublishRequest;

public class PublishRequestArgumentMatcher extends ArgumentMatcher<PublishRequest> {

    private final String topicArn;
    private final String subject;
    private final String message;

    public PublishRequestArgumentMatcher(final String topicArn, final String subject, final String message) {
        this.topicArn = topicArn;
        this.subject = subject;
        this.message = message;
    }

    @Override
    public boolean matches(final Object argument) {
        if (argument instanceof PublishRequest) {
            final PublishRequest request = (PublishRequest) argument;

            if (!topicArn.equals(request.getTopicArn())) {
                return false;
            }

            if (!subject.equals(request.getSubject())) {
                return false;
            }

            if (!message.equals(request.getMessage())) {
                return false;
            }

            return true;
        }

        return false;
    }

}
