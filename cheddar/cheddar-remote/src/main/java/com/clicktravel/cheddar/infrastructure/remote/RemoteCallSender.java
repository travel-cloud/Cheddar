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

import com.clicktravel.cheddar.infrastructure.messaging.HoldableMessageSender;
import com.clicktravel.cheddar.infrastructure.messaging.Message;
import com.clicktravel.cheddar.infrastructure.messaging.SimpleMessage;

@Component
public class RemoteCallSender {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HoldableMessageSender holdableMessageSender;

    @Autowired
    public RemoteCallSender(final HoldableMessageSender remoteCallHoldableMessageSender) {
        holdableMessageSender = remoteCallHoldableMessageSender;
    }

    /**
     * Wrap the given {@link RemoteCall} in a {@link Message} and send it to the remote method queue
     * @param remoteCall
     */
    public void sendRemoteCall(final RemoteCall remoteCall) {
        sendDelayedRemoteCall(remoteCall, 0);
    }

    /**
     * Wrap the given {@link RemoteCall} in a {@link Message} and send it to the remote method queue with a delay. The
     * message will not be visible to any consumers for the specified delay duration; this is intended for re-queueing
     * method call to ensure there is a delay before the remote method is retried.
     * @param remoteCall
     * @param delaySeconds Delay duration (in seconds) before queue consumers can read the message
     */
    public void sendDelayedRemoteCall(final RemoteCall remoteCall, final int delaySeconds) {
        logger.debug("Sending remote call; " + remoteCall);
        final Message message = new SimpleMessage(remoteCall.getClass().getSimpleName(), remoteCall.serialize());
        holdableMessageSender.sendDelayedMessage(message, delaySeconds);
    }

    /**
     * Pause remote calls by holding them in a temporary store rather than sending them. This affects future calls to
     * {@link #sendRemoteCall(RemoteCall)} and {@link #sendDelayedRemoteCall(RemoteCall, int)}. Use {@link #resume()} to
     * flush the held messages.
     */
    public void pause() {
        holdableMessageSender.holdMessages();
    }

    /**
     * Flush any held remote calls during pause and resume normal processing of remote calls.
     */
    public void resume() {
        holdableMessageSender.forwardMessages();
    }
}
