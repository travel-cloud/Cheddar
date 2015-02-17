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
package com.clicktravel.cheddar.application.continuation;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.application.tx.Transactional;

/**
 * Manages all {@link Continuation} objects and provides access for the application.</p>
 * 
 */
@Component
public class ContinuationHandler {

    private final ConcurrentHashMap<String, Continuation> continuationMap = new ConcurrentHashMap<>();
    private final ThreadLocal<String> deferContinuationIdForThread = new ThreadLocal<>();
    private final ThreadLocal<String> continueContinuationIdForThread = new ThreadLocal<>();

    /**
     * @return continuationId Id for continuation associated with calling method. This should be called only by methods
     *         annotated {@link DeferResult}.
     */
    public String getContinuationId() {
        final String continuationId = deferContinuationIdForThread.get();
        if (continuationId == null) {
            throw new ContinuationException("Calling method is not annotated @DeferResult");
        }
        return continuationId;
    }

    /**
     * Sets the object that will be returned to the blocked {@link DeferResult} method associated with the specified
     * continuation. This should only be called by methods annotated {@link ContinueResult}. The object will only be
     * passed when the calling method completes. If the calling method is annotated {@link Transactional}, its
     * transaction must also commit successfully to allow the object to be passed.
     * @param continuationId Id of continuation to pass result to
     * @param result Object to pass as return value
     */
    public void setSimpleMethodResult(final String continuationId, final Object result) {
        setMethodResult(continuationId, new SimpleResult(result));
    }

    /**
     * Sets the exception result that will be thrown to the blocked {@link DeferResult} method associated with the
     * specified continuation. This should only be called by methods annotated {@link ContinueResult}. The exception
     * will only be thrown when the calling method completes. If the calling method is annotated {@link Transactional},
     * its transaction must also commit successfully to allow the exception to be thrown.
     * @param continuationId Id of continuation to pass result to
     * @param exception Exception to pass as thrown exception
     */
    public void setExceptionMethodResult(final String continuationId, final Exception exception) {
        setMethodResult(continuationId, new ExceptionResult(exception));
    }

    void createContinuation() {
        final String continuationId = UUID.randomUUID().toString();
        continuationMap.put(continuationId, new Continuation());
        deferContinuationIdForThread.set(continuationId);
    }

    void removeContinuation() {
        final String continuationId = deferContinuationIdForThread.get();
        deferContinuationIdForThread.remove();
        continuationMap.remove(continuationId);
    }

    private Continuation getContinuation(final String continuationId) {
        final Continuation continuation = continuationMap.get(continuationId);
        if (continuation == null) {
            throw new ContinuationException("@DeferResult method has timed out waiting for method result");
        }
        return continuation;
    }

    private void setMethodResult(final String continuationId, final MethodResult methodResult) {
        continueContinuationIdForThread.set(continuationId);
        final Continuation continuation = getContinuation(continuationId);
        continuation.setMethodResult(methodResult);
    }

    Object pollForMethodReturnValue() throws Exception {
        final String continuationId = deferContinuationIdForThread.get();
        final Continuation continuation = getContinuation(continuationId);
        final MethodResult methodResult = continuation.pollForMethodResult();
        return methodResult.getReturnValue();
    }

    void offerMethodResult() {
        final String continuationId = continueContinuationIdForThread.get();
        if (continuationId != null) { // setMethodResult was called
            try {
                final Continuation continuation = getContinuation(continuationId);
                continuation.offerMethodResult();
            } finally {
                continueContinuationIdForThread.remove();
            }
        }
    }

    void abortOfferMethodResult() {
        continueContinuationIdForThread.remove();
    }
}
