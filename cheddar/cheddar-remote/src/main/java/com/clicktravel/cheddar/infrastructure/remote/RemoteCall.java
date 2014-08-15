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

import com.clicktravel.common.random.Randoms;
import com.thoughtworks.xstream.XStream;

/**
 * Describes a call, with parameter values, on a method in a (remote) interface. The remote method call is applied to a
 * target Spring bean that implements the named interface. This class also records the number of attempts remaining to
 * perform the method call without an exception being thrown, before giving up.
 */
public class RemoteCall {

    /** Maximum number of method call attempts with exception as result before giving up */
    private static final int MAX_ATTEMPTS = 5;

    /** Uniquely identifies this remote call instance */
    private String callId;

    /** Fully qualified name of Spring bean interface that declares the remote method */
    private String interfaceName;

    /** Name of remote method */
    private String methodName;

    /** Parameter types of method as declared in Spring bean interface */
    private String[] methodParameterTypes;

    /** Actual parameter values */
    private Object[] parameters;

    /** Number of method call attempts remaining before giving up */
    private int attemptsRemaining;

    /** Principal actor identifier, as defined by security context */
    private String principal;

    /** Tag flag, used for detecting recent processing of tagged remote calls */
    private boolean tag;

    // Used by deserialisation
    @SuppressWarnings("unused")
    private RemoteCall() {
    }

    public RemoteCall(final String interfaceName, final String methodName, final String[] methodParameterTypes,
            final Object[] parameters, final String principal, final boolean tag) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.methodParameterTypes = methodParameterTypes;
        this.parameters = parameters;
        this.principal = principal;
        this.tag = tag;
        callId = Randoms.randomId();
        attemptsRemaining = MAX_ATTEMPTS;
    }

    public String serialize() {
        try {
            final XStream xstream = new XStream();
            final String serialized = xstream.toXML(this);
            return serialized;
        } catch (final Exception e) {
            throw new RemotingException("Could not serialize remote call; callId:[" + callId + "]");
        }
    }

    public static RemoteCall deserialize(final String serialized) {
        try {
            final XStream xstream = new XStream();
            final RemoteCall remoteCall = (RemoteCall) xstream.fromXML(serialized);
            return remoteCall;
        } catch (final Exception e) {
            throw new RemotingException("Could not deserialize remote call: " + serialized);
        }
    }

    public String getCallId() {
        return callId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String[] getMethodParameterTypes() {
        return methodParameterTypes;
    }

    public Object[] getParameters() {
        return parameters;
    }

    public String getPrincipal() {
        return principal;
    }

    public int getAttemptsRemaining() {
        return attemptsRemaining;
    }

    public void decrementAttemptsRemaining() {
        if (attemptsRemaining > 0) {
            attemptsRemaining--;
        }
    }

    public boolean hasTag() {
        return tag;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("callId:[");
        sb.append(callId);
        sb.append("] interface:[");
        sb.append(interfaceName);
        sb.append("] method:[");
        sb.append(methodName);
        sb.append("] attemptsRemaining:[");
        sb.append(attemptsRemaining);
        sb.append("] principal:[");
        sb.append(principal);
        sb.append("]");
        return sb.toString();
    }
}
