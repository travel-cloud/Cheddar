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
package com.clicktravel.cheddar.remote;

public interface ResponseHandler {

    /**
     * Suspend the current remote call returning a response. Any other remote call can resume this call using the
     * returned {@link Continuation}
     * @return Continuation used to resume suspended remote call
     */
    Continuation suspendResponse();

    /**
     * Resume a suspended remote call and send a return value response
     * @param continuation returned when suspending remote call
     * @param returnValue value to send as a response to the suspended remote call
     */
    void resumeResponseWithReturnValue(final Continuation continuation, final Object returnValue);

    /**
     * Resume a suspended remote call and send an exception response
     * @param continuation returned when suspending remote call
     * @param exception exception to send as a response to the suspended remote call
     */
    void resumeResponseWithException(final Continuation continuation, final Throwable exception);
}
