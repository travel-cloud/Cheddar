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

import com.amazonaws.services.sqs.model.Message;
import com.clicktravel.cheddar.infrastructure.messaging.BasicMessage;
import com.clicktravel.cheddar.infrastructure.messaging.SimpleBasicMessage;

public class SqsBasicMessageQueue extends SqsMessageQueue<BasicMessage> {

    public SqsBasicMessageQueue(final SqsQueueResource sqsQueueResource) {
        super(sqsQueueResource);
    }

    @Override
    protected String toSqsMessageBody(final BasicMessage basicMessage) {
        return basicMessage.getBody();
    }

    @Override
    protected BasicMessage toMessage(final Message sqsMessage) {
        return new SimpleBasicMessage(sqsMessage.getBody(), sqsMessage.getMessageId(), sqsMessage.getReceiptHandle());
    }

}
