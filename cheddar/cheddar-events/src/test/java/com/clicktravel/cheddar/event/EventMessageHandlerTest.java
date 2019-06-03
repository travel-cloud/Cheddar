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
package com.clicktravel.cheddar.event;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;
import com.clicktravel.common.random.Randoms;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class EventMessageHandlerTest {

    @Test
    public void shouldHandleDomainEvent_withMessage() throws Exception {
        // Given
        final String testValue = Randoms.randomString(5);
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("testValue", testValue);
        final String serializedEvent = mapper.writeValueAsString(rootNode);

        final TypedMessage message = mock(TypedMessage.class);
        final String eventType = Randoms.randomString(5);
        when(message.getType()).thenReturn(eventType);
        when(message.getPayload()).thenReturn(serializedEvent);

        final TestConcreteEventHandler mockDomainEventHandler = mock(TestConcreteEventHandler.class);

        final TestConcreteEventHandler concreteDomainEventHandler = new TestConcreteEventHandler();
        doReturn(concreteDomainEventHandler.getEventClass()).when(mockDomainEventHandler).getEventClass();

        final EventMessageHandler<Event> eventMessageHandler = new EventMessageHandler<>();
        eventMessageHandler.registerEventHandler(eventType, mockDomainEventHandler);

        // When
        eventMessageHandler.handle(message);

        // Then
        final ArgumentCaptor<TestConcreteEvent> domainEventCaptor = ArgumentCaptor.forClass(TestConcreteEvent.class);
        verify(mockDomainEventHandler).handle(domainEventCaptor.capture());
        assertEquals("testType", domainEventCaptor.getValue().type());
    }

    @Test
    public void shouldRegisterAndInvokeMultipleHandlers_withMessage() throws Exception {
        // Given
        final String testValue = Randoms.randomString(5);
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("testValue", testValue);
        final String serializedEvent = mapper.writeValueAsString(rootNode);

        final TypedMessage message = mock(TypedMessage.class);
        final String eventType = Randoms.randomString(5);
        when(message.getType()).thenReturn(eventType);
        when(message.getPayload()).thenReturn(serializedEvent);

        final TestConcreteEventHandler mockDomainEventHandler1 = mock(TestConcreteEventHandler.class);
        final TestConcreteEventHandler concreteDomainEventHandler = new TestConcreteEventHandler();
        doReturn(concreteDomainEventHandler.getEventClass()).when(mockDomainEventHandler1).getEventClass();

        final TestOtherConcreteEventHandler mockDomainEventHandler2 = mock(TestOtherConcreteEventHandler.class);
        final TestOtherConcreteEventHandler concreteOtherDomainEventHandler = new TestOtherConcreteEventHandler();
        doReturn(concreteOtherDomainEventHandler.getEventClass()).when(mockDomainEventHandler2).getEventClass();

        final EventMessageHandler<Event> eventMessageHandler = new EventMessageHandler<>();
        eventMessageHandler.registerEventHandler(eventType, mockDomainEventHandler1);
        eventMessageHandler.registerEventHandler(eventType, mockDomainEventHandler2);

        // When
        eventMessageHandler.handle(message);

        // Then
        verify(mockDomainEventHandler1).handle(any(TestConcreteEvent.class));
        verify(mockDomainEventHandler2).handle(any(TestConcreteEvent.class));
    }

    @Test
    public void shouldRegsisterAndInvokeAllMultipleHandlers_withMessageAndHandleException() throws Exception {
        // Given
        final String testValue = Randoms.randomString(5);
        final ObjectMapper mapper = new ObjectMapper();
        final ObjectNode rootNode = mapper.createObjectNode();
        rootNode.put("testValue", testValue);
        final String serializedEvent = mapper.writeValueAsString(rootNode);

        final TypedMessage message = mock(TypedMessage.class);
        final String eventType = Randoms.randomString(5);
        when(message.getType()).thenReturn(eventType);
        when(message.getPayload()).thenReturn(serializedEvent);

        final TestConcreteEventHandler mockDomainEventHandler1 = mock(TestConcreteEventHandler.class);
        final TestConcreteEventHandler concreteDomainEventHandler = new TestConcreteEventHandler();
        doReturn(concreteDomainEventHandler.getEventClass()).when(mockDomainEventHandler1).getEventClass();

        final TestOtherConcreteEventHandler mockDomainEventHandler2 = mock(TestOtherConcreteEventHandler.class);
        final TestOtherConcreteEventHandler concreteOtherDomainEventHandler = new TestOtherConcreteEventHandler();
        doReturn(concreteOtherDomainEventHandler.getEventClass()).when(mockDomainEventHandler2).getEventClass();
        doThrow(IllegalStateException.class).when(mockDomainEventHandler2).handle(any(Event.class));

        final EventMessageHandler<Event> eventMessageHandler = new EventMessageHandler<>();
        eventMessageHandler.registerEventHandler(eventType, mockDomainEventHandler1);
        eventMessageHandler.registerEventHandler(eventType, mockDomainEventHandler2);

        // When
        eventMessageHandler.handle(message);

        // Then
        verify(mockDomainEventHandler1).handle(any(TestConcreteEvent.class));
    }
}
