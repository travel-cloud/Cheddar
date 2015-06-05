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
package com.clicktravel.cheddar.application.pending.result;

import static com.clicktravel.common.random.Randoms.randomId;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.clicktravel.cheddar.system.event.SystemEvent;
import com.thoughtworks.xstream.XStream;

public class PendingResultSetEventHandlerTest {

    private PendingResultSetEventHandler pendingResultSetEventHandler;
    private PendingResultsHolder mockPendingResultsHolder;
    private String applicationName;
    private String applicationVersion;

    @Before
    public void setUp() {
        applicationName = randomString(10);
        applicationVersion = randomString(10);
        mockPendingResultsHolder = mock(PendingResultsHolder.class);
        pendingResultSetEventHandler = new PendingResultSetEventHandler(applicationName, applicationVersion,
                mockPendingResultsHolder);
    }

    @Test
    public void shouldOfferResult_onHandleEvent() throws Exception {
        // Given
        final String value = randomString();
        final Result result = new SimpleResult(value);
        final String resultXml = new XStream().toXML(result);
        final String pendingResultId = randomId();
        final PendingResultSetEvent event = mock(PendingResultSetEvent.class);
        when(event.getPendingResultId()).thenReturn(pendingResultId);
        when(event.getResultXml()).thenReturn(resultXml);
        final PendingResult mockPendingResult = mock(PendingResult.class);
        when(mockPendingResultsHolder.get(pendingResultId)).thenReturn(mockPendingResult);

        // When
        pendingResultSetEventHandler.handleSystemEvent(event);

        // Then
        final ArgumentCaptor<Result> captor = ArgumentCaptor.forClass(Result.class);
        verify(mockPendingResult).offerResult(captor.capture());
        assertTrue(captor.getValue() instanceof SimpleResult);
        final SimpleResult actualResult = (SimpleResult) captor.getValue();
        assertEquals(value, actualResult.getValue());
    }

    @Test
    public void shouldDoNothing_onHandleEventForUnknownPendingResult() {
        // Given
        final String pendingResultId = randomId();
        final PendingResultSetEvent event = mock(PendingResultSetEvent.class);
        when(event.getPendingResultId()).thenReturn(pendingResultId);

        // When
        Exception thrownException = null;
        try {
            pendingResultSetEventHandler.handleSystemEvent(event);
        } catch (final Exception e) {
            thrownException = e;
        }

        // Then
        assertNull(thrownException);
    }

    @Test
    public void shouldReturnEventClass() {
        // When
        final Class<? extends SystemEvent> eventClass = pendingResultSetEventHandler.getEventClass();

        // Then
        assertEquals(PendingResultSetEvent.class, eventClass);
    }
}
