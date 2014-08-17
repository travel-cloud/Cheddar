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
package com.clicktravel.cheddar.domain.event;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.common.validation.ValidationException;
import com.clicktravel.cheddar.domain.model.exception.ConstraintViolationException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DomainEventPublisher.class })
public class ConstraintViolationDomainEventPublisherTest {

    private DomainEvent mockDomainEvent;
    private ConstraintViolationException mockConstraintViolationException;
    private DomainEventPublisher mockDomainEventPublisher;

    @Before
    public void setUp() {
        mockDomainEventPublisher = mock(DomainEventPublisher.class);
        mockStatic(DomainEventPublisher.class);
        when(DomainEventPublisher.instance()).thenReturn(mockDomainEventPublisher);
        mockDomainEvent = mock(DomainEvent.class);
        mockConstraintViolationException = mock(ConstraintViolationException.class);
    }

    @Test
    public void shouldPublishEvent_givenConstraintViolationwithEvent() {
        // Given
        when(mockConstraintViolationException.getDomainEvent()).thenReturn(mockDomainEvent);
        ConstraintViolationDomainEventPublisher publisher = new ConstraintViolationDomainEventPublisher();

        // When
        publisher.handle(mockConstraintViolationException);

        // Then
        verify(mockDomainEventPublisher).publishEvent(mockDomainEvent);
    }

    @Test
    public void shouldNotPublishEvent_givenConstraintViolationwithoutEvent() {
        // Given
        ConstraintViolationDomainEventPublisher publisher = new ConstraintViolationDomainEventPublisher();

        // When
        publisher.handle(mockConstraintViolationException);

        // Then
        verifyZeroInteractions(mockDomainEventPublisher);
    }

    @Test
    public void shouldNotPublishEvent_givenOtherException() {
        // Given
        ConstraintViolationDomainEventPublisher publisher = new ConstraintViolationDomainEventPublisher();
        ValidationException mockValidationException = mock(ValidationException.class);

        // When

        publisher.handle(mockValidationException);

        // Then
        verifyZeroInteractions(mockDomainEventPublisher);
    }
}
