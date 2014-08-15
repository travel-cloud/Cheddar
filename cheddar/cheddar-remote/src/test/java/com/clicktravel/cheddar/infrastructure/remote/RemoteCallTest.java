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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import com.clicktravel.common.random.Randoms;
import com.clicktravel.cheddar.infrastructure.remote.RemoteCall;

public class RemoteCallTest {
    @Test
    public void shouldDecrementAttemptsRemaining() {
        // Given
        final RemoteCall remoteCall = randomMethod1RemoteCall();

        // When
        final int originalAttemptsRemaining = remoteCall.getAttemptsRemaining();
        remoteCall.decrementAttemptsRemaining();

        // Then
        assertTrue(originalAttemptsRemaining > 0);
        assertEquals(originalAttemptsRemaining - 1, remoteCall.getAttemptsRemaining());
    }

    @Test
    public void shouldRoundTripSerialization_withTestMethod1() {
        // Given
        final RemoteCall remoteCall = randomMethod1RemoteCall();

        checkRoundTripSerialization(remoteCall);
    }

    @Test
    public void shouldRoundTripSerialization_withTestMethod2() {
        // Given
        final RemoteCall remoteCall = randomMethod2RemoteCall();

        checkRoundTripSerialization(remoteCall);
    }

    private void checkRoundTripSerialization(final RemoteCall remoteCall) {
        // When
        final String serialized = remoteCall.serialize();
        final RemoteCall deserialized = RemoteCall.deserialize(serialized);

        // Then
        assertEquals(remoteCall.getCallId(), deserialized.getCallId());
        assertEquals(remoteCall.getInterfaceName(), deserialized.getInterfaceName());
        assertEquals(remoteCall.getMethodName(), deserialized.getMethodName());
        assertTrue(Arrays.equals(remoteCall.getMethodParameterTypes(), deserialized.getMethodParameterTypes()));
        assertTrue(Arrays.equals(remoteCall.getParameters(), deserialized.getParameters()));
        assertEquals(remoteCall.getPrincipal(), deserialized.getPrincipal());
        assertEquals(remoteCall.hasTag(), deserialized.hasTag());
    }

    private RemoteCall randomMethod1RemoteCall() {
        final String interfaceName = TestService.class.getName();
        final String methodName = "method1";
        final String[] methodParameterTypes = new String[] { String.class.getName(), int.class.getName() };
        final Object[] parameters = new Object[] { Randoms.randomString(), Randoms.randomInt(30) };
        final String principal = Randoms.randomString();
        final boolean tag = Randoms.randomBoolean();
        return new RemoteCall(interfaceName, methodName, methodParameterTypes, parameters, principal, tag);
    }

    private RemoteCall randomMethod2RemoteCall() {
        final TestObject testObject = new TestObject(Randoms.randomString(), Randoms.randomString());
        final String interfaceName = TestService.class.getName();
        final String methodName = "method2";
        final String[] methodParameterTypes = new String[] { TestObject.class.getName() };
        final Object[] parameters = new Object[] { testObject };
        final String principal = Randoms.randomString();
        final boolean tag = Randoms.randomBoolean();
        return new RemoteCall(interfaceName, methodName, methodParameterTypes, parameters, principal, tag);
    }

}
