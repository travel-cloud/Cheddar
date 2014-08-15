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

import com.thoughtworks.xstream.XStream;

/**
 * Describes the response after invoking a remote method specified by an associated {@link RemoteCall}.
 */
public class RemoteResponse {

    private String callId;
    private Object returnValue;
    private Throwable thrownException;

    // Used by deserialisation
    @SuppressWarnings("unused")
    private RemoteResponse() {
    }

    public RemoteResponse(final String callId) {
        this.callId = callId;
    }

    public String serialize() {
        try {
            XStream xstream = new XStream();
            String serialized = xstream.toXML(this);
            return serialized;
        } catch (Exception e) {
            throw new RemotingException("Could not serialize remote response; callId:[" + callId + "]");
        }
    }

    public static RemoteResponse deserialize(final String serialized) {
        try {
            XStream xstream = new XStream();
            RemoteResponse remoteResponse = (RemoteResponse) xstream.fromXML(serialized);
            return remoteResponse;
        } catch (Exception e) {
            throw new RemotingException("Could not deserialize remote response: " + serialized);
        }
    }

    public Object returnValue() throws Throwable {
        if (thrownException != null) {
            throw thrownException;
        }
        return returnValue;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(final Object returnValue) {
        this.returnValue = returnValue;
    }

    public Throwable getThrownException() {
        return thrownException;
    }

    public void setThrownException(final Throwable thrownException) {
        this.thrownException = thrownException;
    }

    public String getCallId() {
        return callId;
    }

}
