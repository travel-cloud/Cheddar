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
package com.clicktravel.cheddar.server.application.lifecycle;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.system.event.ApplicationEC2InstanceTerminationRequestedEvent;
import com.clicktravel.cheddar.system.event.SystemEvent;

public class ApplicationEC2InstanceTerminationRequestedEventHandlerTest {

    private ApplicationEC2InstanceTerminationRequestedEventHandler handler;
    private String ec2InstanceId;
    private ApplicationLifecycleController mockApplicationLifecycleController;

    @Before
    public void setUp() {
        ec2InstanceId = randomString();
        mockApplicationLifecycleController = mock(ApplicationLifecycleController.class);
        handler = new ApplicationEC2InstanceTerminationRequestedEventHandler(ec2InstanceId,
                mockApplicationLifecycleController);
    }

    @Test
    public void shouldRequestApplicationShutdown_withEventForEc2Instance() throws Exception {
        // Given
        final ApplicationEC2InstanceTerminationRequestedEvent event = new ApplicationEC2InstanceTerminationRequestedEvent();
        event.setEc2InstanceId(ec2InstanceId);

        // When
        handler.handle(event);

        // Then
        Thread.sleep(100);
        verify(mockApplicationLifecycleController).shutdownApplication();
    }

    @Test
    public void shouldNotRequestApplicationShutdown_withEventForOtherEc2Instance() throws Exception {
        // Given
        final ApplicationEC2InstanceTerminationRequestedEvent event = new ApplicationEC2InstanceTerminationRequestedEvent();
        event.setEc2InstanceId(randomString());

        // When
        handler.handle(event);

        // Then
        Thread.sleep(100);
        verifyNoMoreInteractions(mockApplicationLifecycleController);
    }

    @Test
    public void shouldReturnEventClass() {
        // When
        final Class<? extends SystemEvent> returnedClass = handler.getEventClass();

        // Then
        assertEquals(ApplicationEC2InstanceTerminationRequestedEvent.class, returnedClass);
    }
}
