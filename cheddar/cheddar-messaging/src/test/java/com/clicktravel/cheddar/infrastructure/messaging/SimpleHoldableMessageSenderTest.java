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
package com.clicktravel.cheddar.infrastructure.messaging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.common.random.Randoms;

public class SimpleHoldableMessageSenderTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final int NUM_SENDER_THREADS = 5;
    private static final int NUM_MESSAGES_PER_THREAD = 100;
    private int messagesUntilToggle;
    private boolean holdingMessages;

    private SimpleHoldableMessageSender simpleHoldableMessageSender;
    private MessageSender mockMessageSender;
    private InOrder inOrder;
    private final List<Message> mockMessages = new ArrayList<>();

    @Before
    public void setUp() {
        mockMessageSender = mock(MessageSender.class);
        inOrder = Mockito.inOrder(mockMessageSender);
        final int numMessages = 2 + Randoms.randomInt(5);
        for (int n = 0; n < numMessages; n++) {
            mockMessages.add(mock(Message.class));
        }
        simpleHoldableMessageSender = new SimpleHoldableMessageSender(mockMessageSender);
    }

    @Test
    public void shouldSendMessagesAfterConstruction_onSend() {
        // When
        for (final Message mockMessage : mockMessages) {
            simpleHoldableMessageSender.sendMessage(mockMessage);
        }

        // Then
        for (final Message message : mockMessages) {
            inOrder.verify(mockMessageSender).sendDelayedMessage(message, 0);
        }
    }

    @Test
    public void shouldSendDelayedMessagesAfterConstruction_onSend() {
        // Given
        final int delay = Randoms.randomInt(10);

        // When
        for (final Message mockMessage : mockMessages) {
            simpleHoldableMessageSender.sendDelayedMessage(mockMessage, delay);
        }

        // Then
        for (final Message mockMessage : mockMessages) {
            inOrder.verify(mockMessageSender).sendDelayedMessage(mockMessage, delay);
        }
    }

    @Test
    public void shouldNotSendMessagesAfterHold_onSend() {
        // Given
        simpleHoldableMessageSender.holdMessages();

        // When
        for (final Message mockMessage : mockMessages) {
            simpleHoldableMessageSender.sendMessage(mockMessage);
        }

        // Then
        verifyNoMoreInteractions(mockMessageSender);
    }

    @Test
    public void shouldSendHeldMessages_onForwardMessages() {
        // Given
        simpleHoldableMessageSender.holdMessages();
        for (final Message mockMessage : mockMessages) {
            simpleHoldableMessageSender.sendMessage(mockMessage);
        }

        // When
        simpleHoldableMessageSender.forwardMessages();

        // Then
        for (final Message mockMessage : mockMessages) {
            inOrder.verify(mockMessageSender).sendDelayedMessage(mockMessage, 0);
        }
    }

    @Test
    public void shouldBeThreadsafe_onSendHoldAndForward() throws Exception {
        // Given
        final List<List<Message>> messagesByThread = new ArrayList<>(NUM_SENDER_THREADS);
        for (int n = 0; n < NUM_SENDER_THREADS; n++) {
            final List<Message> messages = new ArrayList<>(NUM_MESSAGES_PER_THREAD);
            for (int m = 0; m < NUM_MESSAGES_PER_THREAD; m++) {
                messages.add(mock(Message.class));
            }
            messagesByThread.add(messages);
        }
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch stopLatch = new CountDownLatch(NUM_SENDER_THREADS);
        for (final List<Message> messages : messagesByThread) {
            new MessageSenderTask(messages, startLatch, stopLatch).start();
        }
        messagesUntilToggle = 10 + Randoms.randomInt(90);

        // When
        startLatch.countDown(); // Start all threads
        stopLatch.await(); // Wait for all threads to complete
        simpleHoldableMessageSender.forwardMessages(); // Flush any held messages

        // Then
        for (final List<Message> threadMessages : messagesByThread) {
            for (final Message message : threadMessages) {
                verify(mockMessageSender).sendDelayedMessage(message, 0);
            }
        }
        verifyNoMoreInteractions(mockMessageSender);
    }

    private class MessageSenderTask extends Thread {

        private final List<Message> messagesToSend;
        private final CountDownLatch startLatch;
        private final CountDownLatch stopLatch;

        MessageSenderTask(final List<Message> messagesToSend, final CountDownLatch startLatch,
                final CountDownLatch stopLatch) {
            this.messagesToSend = messagesToSend;
            this.startLatch = startLatch;
            this.stopLatch = stopLatch;
        }

        @Override
        public void run() {
            try {
                startLatch.await();
            } catch (final InterruptedException e) {
                // Do nothing
            }

            logger.debug("Sending thread started");
            for (final Message message : messagesToSend) {
                simpleHoldableMessageSender.sendMessage(message);
                randomToggle();
            }

            logger.debug("Sending thread finished");
            stopLatch.countDown();
        }
    }

    private synchronized void randomToggle() {
        if (--messagesUntilToggle == 0) {
            messagesUntilToggle = 10 + Randoms.randomInt(90);
            if (holdingMessages) {
                logger.debug("Forwarding messages");
                simpleHoldableMessageSender.forwardMessages();
                holdingMessages = false;
            } else {
                logger.debug("Holding messages");
                simpleHoldableMessageSender.holdMessages();
                holdingMessages = true;
            }
        }
    }
}
