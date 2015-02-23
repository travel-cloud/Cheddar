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
package com.clicktravel.infrastructure.messaging.inmemory;

import java.util.Collection;
import java.util.List;

import com.clicktravel.cheddar.event.AbstractEvent;
import com.clicktravel.cheddar.event.Event;
import com.clicktravel.cheddar.infrastructure.messaging.TypedMessage;

/**
 * Methods that verify that certain messages have been sent or events published to in-memory mocks. These methods can be
 * used to verify in-memory messages sent to a {@link InMemoryMessageSender} or event messages published using a
 * {@link InMemoryMessagePublisher}
 */
public class InMemoryMessageVerifier {

    /**
     * Verifies that a message of a specified type has been sent using the given {@link InMemoryMessageSender}
     * @param inMemoryMessageSender {@link InMemoryMessageSender} to check
     * @param messageType Type of message
     * @return {@code true} if message of the specified type was sent
     */
    public static boolean messageTypeSent(final InMemoryMessageSender<TypedMessage> inMemoryMessageSender,
            final String messageType) {
        return containsMessageOfType(inMemoryMessageSender.getSentMessages(), messageType);
    }

    /**
     * Verifies that an event of a specified class has been published using the given {@link InMemoryMessagePublisher}
     * @param inMemoryMessagePublisher {@link InMemoryMessagePublisher} to check
     * @param eventClass Class of event
     * @return {@code true} if event of specified class has been published
     */
    public static boolean eventTypePublished(final InMemoryMessagePublisher<TypedMessage> inMemoryMessagePublisher,
            final Class<? extends Event> eventClass) {
        try {
            final String messageType = eventClass.newInstance().type();
            return containsMessageOfType(inMemoryMessagePublisher.getPublishedMessages(), messageType);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to instantiate prototype event to verify publication");
        }
    }

    /**
     * Verifies that an event equals to the one specified has been published using the given
     * {@link InMemoryMessagePublisher}
     * @param inMemoryMessagePublisher {@link InMemoryMessagePublisher} to check
     * @param expectedEvent Event to match
     * @return {@code true} if matching event has been published
     */
    public static boolean eventPublished(final InMemoryMessagePublisher<TypedMessage> inMemoryMessagePublisher,
            final Event expectedEvent) {
        return containsSerializedEvent(inMemoryMessagePublisher.getPublishedMessages(), expectedEvent);
    }

    private static boolean containsSerializedEvent(final List<TypedMessage> typedMessages, final Event expectedEvent) {
        for (final TypedMessage typedMessage : typedMessages) {
            if (typedMessage.getType().equals(expectedEvent.type())) {
                final Event event = AbstractEvent.newEvent(expectedEvent.getClass(), typedMessage.getPayload());
                if (expectedEvent.equals(event)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean containsMessageOfType(final Collection<TypedMessage> messages,
            final String expectedMessageType) {
        for (final TypedMessage message : messages) {
            if (message.getType().equals(expectedMessageType)) {
                return true;
            }
        }
        return false;
    }
}
