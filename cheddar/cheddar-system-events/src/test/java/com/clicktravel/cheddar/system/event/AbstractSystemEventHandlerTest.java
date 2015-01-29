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
package com.clicktravel.cheddar.system.event;

import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

public class AbstractSystemEventHandlerTest {

    @Test
    public void shouldHandleEvent_withCorrectApplicationName() throws Exception {
        // Given
        final String applicationName = randomString(10);
        final String applicationVersion = randomString(10);
        final SystemEvent event = mock(SystemEvent.class);
        when(event.getTargetApplicationName()).thenReturn(applicationName);

        final AbstractSystemEventHandlerStub handler = new AbstractSystemEventHandlerStub(applicationName,
                applicationVersion);

        // When
        handler.handle(event);

        // Then
        assertTrue(handler.handled());
    }

    @Test
    public void shouldHandleEvent_withCorrectApplicationNameAndVersion() throws Exception {
        // Given
        final String applicationName = randomString(10);
        final String applicationVersion = randomString(10);
        final SystemEvent event = mock(SystemEvent.class);
        when(event.getTargetApplicationName()).thenReturn(applicationName);
        when(event.getTargetApplicationVersion()).thenReturn(applicationVersion);

        final AbstractSystemEventHandlerStub handler = new AbstractSystemEventHandlerStub(applicationName,
                applicationVersion);

        // When
        handler.handle(event);

        // Then
        assertTrue(handler.handled());
    }

}
