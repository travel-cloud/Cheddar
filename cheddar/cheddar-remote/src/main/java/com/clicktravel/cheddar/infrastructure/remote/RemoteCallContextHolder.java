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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.remote.Continuation;
import com.clicktravel.cheddar.remote.ResponseHandler;

/**
 * Allows a remote call to suspend returning a response; Also allows a remote call to send a response on behalf of a
 * call that has suspended its response.
 */
@Component
public class RemoteCallContextHolder implements ResponseHandler {

    private final static ThreadLocal<RemoteCallContext> context = new ThreadLocal<RemoteCallContext>() {
    };

    private final RemoteResponseSender remoteResponseSender;

    @Autowired
    public RemoteCallContextHolder(final RemoteResponseSender remoteResponseSender) {
        this.remoteResponseSender = remoteResponseSender;
    }

    /**
     * Establish remote call context. This is done before the remote call method executes.
     * @param remoteCall
     */
    void initialiseContext(final RemoteCall remoteCall) {
        context.set(new RemoteCallContext(remoteCall));
    }

    /**
     * Tear down remote call context. This is done sometime after the remote call method executes.
     */
    void clearContext() {
        context.remove();
    }

    /**
     * @return <code>true</code> if remote call method has suspended its response
     */
    boolean isResponseSuspended() {
        final RemoteCallContext remoteCallContext = context.get();
        return remoteCallContext.isResponseSuspended();
    }

    @Override
    public Continuation suspendResponse() {
        final RemoteCallContext remoteCallContext = context.get();
        remoteCallContext.setResponseSuspended(true);
        final Continuation continuation = new Continuation(remoteCallContext.getRemoteCall().getCallId());
        return continuation;
    }

    @Override
    public void resumeResponseWithReturnValue(final Continuation continuation, final Object returnValue) {
        final RemoteResponse remoteResponse = new RemoteResponse(continuation.getCallId());
        remoteResponse.setReturnValue(returnValue);
        remoteResponseSender.sendRemoteResponse(remoteResponse);
    }

    @Override
    public void resumeResponseWithException(final Continuation continuation, final Throwable exception) {
        final RemoteResponse remoteResponse = new RemoteResponse(continuation.getCallId());
        remoteResponse.setThrownException(exception);
        remoteResponseSender.sendRemoteResponse(remoteResponse);
    }
}
