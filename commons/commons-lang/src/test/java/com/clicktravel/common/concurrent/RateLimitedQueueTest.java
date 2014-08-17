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
package com.clicktravel.common.concurrent;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.clicktravel.common.random.Randoms;

public class RateLimitedQueueTest {

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldTakeElement_whenLimiterAllows() throws Exception {
        // Given
        @SuppressWarnings("unchecked")
        final BlockingQueue<Object> mockQueue = mock(BlockingQueue.class);
        final Object element = new Object();
        when(mockQueue.take()).thenReturn(element);
        final RateLimiter mockRateLimiter = mock(RateLimiter.class);
        final RateLimitedQueue<Object> rateLimitedQueue = new RateLimitedQueue<>(mockQueue, mockRateLimiter);

        // When
        final Object returnedElement = rateLimitedQueue.take();

        // Then
        verify(mockRateLimiter).takeToken();
        assertSame(element, returnedElement);
    }

    @Test
    public void shouldPollElement_whenLimiterAllows() throws Exception {
        // Given
        final long startTime = Randoms.randomDateTime().getMillis();
        final long getTokenElapsedMillis = Randoms.randomInt(1000);
        final long timeoutMillis = 2000 + Randoms.randomInt(1000);
        final long expectedPollNanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis - getTokenElapsedMillis);
        DateTimeUtils.setCurrentMillisFixed(startTime);

        @SuppressWarnings("unchecked")
        final BlockingQueue<Object> mockQueue = mock(BlockingQueue.class);
        final Object element = new Object();
        when(mockQueue.poll(expectedPollNanos, TimeUnit.NANOSECONDS)).thenReturn(element);
        final RateLimiter mockRateLimiter = mock(RateLimiter.class);
        when(mockRateLimiter.pollToken(timeoutMillis, TimeUnit.MILLISECONDS)).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(final InvocationOnMock invocation) throws Throwable {
                DateTimeUtils.setCurrentMillisFixed(startTime + getTokenElapsedMillis);
                return true;
            }
        });
        final RateLimitedQueue<Object> rateLimitedQueue = new RateLimitedQueue<>(mockQueue, mockRateLimiter);

        // When
        final Object returnedElement = rateLimitedQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS);

        // Then
        assertSame(element, returnedElement);
    }

    @Test
    public void shouldReturnNull_whenPollTokenTimesOut() throws Exception {
        // Given
        final long timeout = 10 + Randoms.randomInt(1000);
        final TimeUnit timeoutUnit = Randoms.randomEnum(TimeUnit.class);
        @SuppressWarnings("unchecked")
        final BlockingQueue<Object> mockQueue = mock(BlockingQueue.class);
        final RateLimiter mockRateLimiter = mock(RateLimiter.class);
        when(mockRateLimiter.pollToken(timeout, timeoutUnit)).thenReturn(false);
        final RateLimitedQueue<Object> rateLimitedQueue = new RateLimitedQueue<>(mockQueue, mockRateLimiter);

        // When
        final Object element = rateLimitedQueue.poll(timeout, timeoutUnit);

        // Then
        verify(mockRateLimiter).pollToken(timeout, timeoutUnit);
        assertNull(element);
    }
}
