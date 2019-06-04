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
import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.system.event.publisher.SystemEventPublisher;
import com.thoughtworks.xstream.XStream;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SystemEventPublisher.class)
@PowerMockIgnore("javax.xml.*")
public class PendingResultHandlerTest {

    private PendingResultHandler pendingResultHandler;
    private String applicationName;
    private PendingResultsHolder mockPendingResultsHolder;
    private SystemEventPublisher mockSystemEventPublisher;

    @BeforeClass
    public static void classSetup() {
        mockStatic(SystemEventPublisher.class);
    }

    @Before
    public void setUp() {
        applicationName = randomString(10);
        mockPendingResultsHolder = mock(PendingResultsHolder.class);
        mockSystemEventPublisher = mock(SystemEventPublisher.class);
        mockStatic(SystemEventPublisher.class);
        when(SystemEventPublisher.instance()).thenReturn(mockSystemEventPublisher);
        pendingResultHandler = new PendingResultHandler(mockPendingResultsHolder, applicationName);
    }

    @Test
    public void shouldPublishEvent_onOfferValue() throws Exception {
        // Given
        final TestValue testValue = new TestValue(randomString(), randomInt(100));
        final String pendingResultId = randomId();

        // When
        pendingResultHandler.offerValue(pendingResultId, testValue);

        // Then
        final ArgumentCaptor<PendingResultOfferedEvent> captor = ArgumentCaptor.forClass(PendingResultOfferedEvent.class);
        verify(mockSystemEventPublisher).publishEvent(captor.capture());
        final PendingResultOfferedEvent event = captor.getValue();
        assertEquals(applicationName, event.getTargetApplicationName());
        assertNull(event.getTargetApplicationVersion());
        assertEquals(pendingResultId, event.getPendingResultId());
        final Object actualResult = new XStream().fromXML(event.getResultXml());
        assertTrue(actualResult instanceof Result);
        assertEquals(((Result) actualResult).getValue(), testValue);
    }

    @Test
    public void shouldPublishEvent_onOfferNullValue() throws Exception {
        // Given
        final Object value = null;
        final String pendingResultId = randomId();

        // When
        pendingResultHandler.offerValue(pendingResultId, value);

        // Then
        final ArgumentCaptor<PendingResultOfferedEvent> captor = ArgumentCaptor.forClass(PendingResultOfferedEvent.class);
        verify(mockSystemEventPublisher).publishEvent(captor.capture());
        final PendingResultOfferedEvent event = captor.getValue();
        assertEquals(applicationName, event.getTargetApplicationName());
        assertNull(event.getTargetApplicationVersion());
        assertEquals(pendingResultId, event.getPendingResultId());
        final Object actualResult = new XStream().fromXML(event.getResultXml());
        assertTrue(actualResult instanceof Result);
        assertNull(((Result) actualResult).getValue());
    }

    @Test
    public void shouldPublishEvent_onOfferException() throws Exception {
        // Given
        final Exception exception = new IllegalStateException(randomString());
        final String pendingResultId = randomId();

        // When
        pendingResultHandler.offerException(pendingResultId, exception);

        // Then
        final ArgumentCaptor<PendingResultOfferedEvent> captor = ArgumentCaptor.forClass(PendingResultOfferedEvent.class);
        verify(mockSystemEventPublisher).publishEvent(captor.capture());
        final PendingResultOfferedEvent event = captor.getValue();
        assertEquals(applicationName, event.getTargetApplicationName());
        assertNull(event.getTargetApplicationVersion());
        assertEquals(pendingResultId, event.getPendingResultId());
        final Object actualResult = new XStream().fromXML(event.getResultXml());
        assertTrue(actualResult instanceof Result);
        IllegalStateException thrownException = null;
        try {
            ((Result) actualResult).getValue();
        } catch (final IllegalStateException e) {
            thrownException = e;
        }
        assertNotNull(thrownException);
        assertEquals(thrownException.getMessage(), exception.getMessage());
    }

    @Test
    public void shouldCreatePendingResult() {
        // Given
        final String pendingResultId = randomId();
        when(mockPendingResultsHolder.create()).thenReturn(pendingResultId);

        // When
        final String returnedId = pendingResultHandler.createPendingResult();

        // Then
        assertEquals(pendingResultId, returnedId);
    }

    @Test
    public void shouldRemovePendingResult_withPendingResultId() {
        // Given
        final String pendingResultId = randomId();

        // When
        pendingResultHandler.removePendingResult(pendingResultId);

        // Then
        verify(mockPendingResultsHolder).remove(pendingResultId);
    }

    @Test
    public void shouldPollValue_withPendingResultId() throws Exception {
        // Given
        final Object value = new Object();
        final Result result = mock(Result.class);
        when(result.getValue()).thenReturn(value);
        final PendingResult mockPendingResult = mock(PendingResult.class);
        when(mockPendingResult.pollResult()).thenReturn(result);
        final String pendingResultId = randomId();
        when(mockPendingResultsHolder.get(pendingResultId)).thenReturn(mockPendingResult);

        // When
        final Object returnedValue = pendingResultHandler.pollValue(pendingResultId);

        // Then
        assertNotNull(returnedValue);
        assertSame(value, returnedValue);
    }
}
