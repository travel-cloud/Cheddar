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
package com.clicktravel.infrastructure.messaging.aws.sqs;

import org.mockito.ArgumentMatcher;

import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SendMessageRequestArgumentMatcher extends ArgumentMatcher<SendMessageRequest> {

    private final String message;
    private final String queueUrl;

    public SendMessageRequestArgumentMatcher(final String message, final String queueUrl) {
        this.message = message;
        this.queueUrl = queueUrl;
    }

    @Override
    public boolean matches(final Object argument) {
        if (argument instanceof SendMessageRequest) {
            final SendMessageRequest request = (SendMessageRequest) argument;

            if (!message.equals(request.getMessageBody())) {
                return false;
            }

            if (!queueUrl.equals(request.getQueueUrl())) {
                return false;
            }

            return true;
        }

        return false;
    }
}
