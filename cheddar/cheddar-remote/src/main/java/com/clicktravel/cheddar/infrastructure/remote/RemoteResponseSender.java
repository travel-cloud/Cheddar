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
package com.clicktravel.cheddar.infrastructure.remote;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.MessageSender;
import com.clicktravel.cheddar.infrastructure.messaging.SimpleMessage;

@Component
public class RemoteResponseSender {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final MessageSender messageSender;

    @Autowired
    public RemoteResponseSender(final MessageSender remoteResponseMessageSender) {
        this.messageSender = remoteResponseMessageSender;
    }

    public void sendRemoteResponse(final RemoteResponse remoteResponse) {
        logger.debug("Sending remote response; callId:[" + remoteResponse.getCallId() + "]");
        final Message message = new SimpleMessage(remoteResponse.getClass().getSimpleName(), remoteResponse.serialize());
        messageSender.sendMessage(message);
    }

}
