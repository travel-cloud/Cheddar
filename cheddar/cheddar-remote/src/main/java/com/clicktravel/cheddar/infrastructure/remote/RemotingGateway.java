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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RemotingGateway {
    private static final long MAX_QUEUE_OFFER_TIMEOUT_SECONDS = 1; // Max wait for threads to synchronise
    private static final long MAX_QUEUE_POLL_TIMEOUT_SECONDS = 40; // Must cover all remote method call retries
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final RemoteCallSender remoteCallSender;

    /**
     * {@link ConcurrentHashMap} from callId (of a {@link RemoteCall} instance) to the {@link SynchronousQueue} used for
     * handing off the {@link RemoteResponse} from the response thread to the caller thread.
     */
    private final ConcurrentHashMap<String, SynchronousQueue<RemoteResponse>> callIdMap = new ConcurrentHashMap<>();

    @Autowired
    public RemotingGateway(final RemoteCallSender remoteCallSender) {
        this.remoteCallSender = remoteCallSender;
    }

    /**
     * Invokes a remote method and either returns a result or throws an exception. This method will block until the
     * command execution completes or an exception is produced.
     * @param remoteCall
     * @return Value returned from command processing
     */
    public Object invokeSynchronously(final RemoteCall remoteCall) throws Throwable {
        sendRemoteCall(remoteCall);
        final RemoteResponse remoteResponse = blockUntilResponse(remoteCall);
        final Object returnValue = remoteResponse.returnValue();
        return returnValue;
    }

    /**
     * Invokes a remote method and returns immediately without returning any result (or thrown exception).
     * @param remoteCall
     */
    public void invokeAsynchronouslyWithoutResponse(final RemoteCall remoteCall) {
        sendRemoteCall(remoteCall);
    }

    private void sendRemoteCall(final RemoteCall remoteCall) {
        remoteCallSender.sendRemoteCall(remoteCall);
    }

    private RemoteResponse blockUntilResponse(final RemoteCall remoteCall) {
        final String callId = remoteCall.getCallId();
        RemoteResponse remoteResponse = null;

        try {
            final SynchronousQueue<RemoteResponse> synchronousQueue = createOrGetRemoteResponseSynchronousQueue(callId);
            remoteResponse = synchronousQueue.poll(MAX_QUEUE_POLL_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            logger.warn("Interrupted while waiting for remote response handoff to caller");
        } finally {
            deleteRemoteResponseSynchronousQueue(callId);
        }

        if (remoteResponse == null) {
            throw new IllegalStateException("No response for remote call; " + remoteCall);
        }
        return remoteResponse;
    }

    /**
     * Handles remote response by handing off to method blocked on response
     * @param remoteResponse
     */
    public void handle(final RemoteResponse remoteResponse) {
        final String callId = remoteResponse.getCallId();
        boolean remoteResponseHandedOffToCaller = false;

        try {
            final SynchronousQueue<RemoteResponse> synchronousQueue = createOrGetRemoteResponseSynchronousQueue(callId);
            remoteResponseHandedOffToCaller = synchronousQueue.offer(remoteResponse, MAX_QUEUE_OFFER_TIMEOUT_SECONDS,
                    TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            logger.warn("Interrupted while handing off remote response to caller");
        } finally {
            deleteRemoteResponseSynchronousQueue(callId);
        }

        if (!remoteResponseHandedOffToCaller) {
            logger.warn("Caller gone away, dropping remote response; callId:[" + remoteResponse.getCallId() + "]");
        }
    }

    /**
     * Ensures that there is a mapping from callId to a {@link SynchronousQueue} instance, creating a new mapping if
     * necessary. This method can be safely used by both command invoker and command response handler threads to agree
     * on a common synchronous queue instance for handing off the command response.
     * @param callId identifying a remote call instance and its associated response
     * @return The {@link SynchronousQueue} instance to use for handing off the remote response
     */
    private SynchronousQueue<RemoteResponse> createOrGetRemoteResponseSynchronousQueue(final String callId) {
        callIdMap.putIfAbsent(callId, new SynchronousQueue<RemoteResponse>());
        final SynchronousQueue<RemoteResponse> queue = callIdMap.get(callId);
        return queue;
    }

    /**
     * Deletes the mapping from callId to its associated {@link SynchronousQueue}, allowing the queue to be garbage
     * collected. This method can be safely called from either (or both) command invoker and command response handler
     * threads, when the queue is no longer needed.
     * @param callId
     */
    private void deleteRemoteResponseSynchronousQueue(final String callId) {
        callIdMap.remove(callId);
    }

}
