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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

import com.clicktravel.common.random.Randoms;

public class RateLimiterTest {

    private RateLimiter rateLimiter;
    private CountDownLatch countDownLatch;

    @Test(timeout = 1000)
    public void shouldReturnImmediately_onGetTokensUpToCapacity() throws Exception {
        // Given
        final int capacity = 50;
        final long tokenDelayMillis = 500;
        rateLimiter = new RateLimiter(capacity, tokenDelayMillis);

        // When
        final long elapsedMillis = timeGetTokens(capacity);

        // Then
        assertTrue(elapsedMillis < tokenDelayMillis);
    }

    @Test(timeout = 1000)
    public void shouldBlock_onGetTokenWhenNoneAvailable() throws Exception {
        // Given
        final int capacity = 50;
        final long tokenDelayMillis = 500;
        rateLimiter = new RateLimiter(capacity, tokenDelayMillis);

        // When
        final long elapsedMillis = timeGetTokens(capacity + 1);

        // Then
        assertTrue(elapsedMillis >= tokenDelayMillis);
    }

    @Test(timeout = 2000)
    public void shouldLimitRate_onGetTokens() throws Exception {
        // Given

        // Use a low capacity for this test, to smooth actual rate
        // Tokens will effectively become available in groups of 2
        // Rate is 2 tokens per 30ms, which is 66.6 tokens per second
        final int capacity = 2;
        final long tokenDelayMillis = 30;

        // Sample the rate by timing getting a batch of tokens
        // Actual timing may differ from ideal, up to approximately the token delay
        final int numTokensPerSample = 14;
        final long idealSampleElapsedMillis = numTokensPerSample * tokenDelayMillis / capacity; // = 210
        final long maxExpectedVarianceMillis = tokenDelayMillis * 3 / 2; // = 45
        final long minExpectedSampleElapsedMillis = idealSampleElapsedMillis - maxExpectedVarianceMillis; // = 165
        final long maxExpectedSampleElapsedMillis = idealSampleElapsedMillis + maxExpectedVarianceMillis; // = 255
        rateLimiter = new RateLimiter(capacity, tokenDelayMillis);
        spreadGetTokens(capacity, tokenDelayMillis / capacity);

        // When
        final long[] elapsedMillis = new long[5];
        for (int i = 0; i < elapsedMillis.length; i++) {
            elapsedMillis[i] = timeGetTokens(numTokensPerSample);
        }

        // Then
        for (final long elapsed : elapsedMillis) {
            assertTrue("Minimum expected time elapsed to get tokens is " + minExpectedSampleElapsedMillis
                    + "ms but actual was " + elapsed + "ms", elapsed >= minExpectedSampleElapsedMillis);
            assertTrue("Maximum expected time elapsed to get tokens is " + maxExpectedSampleElapsedMillis
                    + "ms but actual was " + elapsed + "ms", elapsed <= maxExpectedSampleElapsedMillis);
        }
    }

    @Test(timeout = 1000)
    public void shouldDoubleRate_onDoubleCapacity() throws Exception {
        // Given initial rate is 5 tokens per 77ms, which is approximately 65 tokens per second
        final int capacity = 5;
        final long tokenDelayMillis = 77;
        rateLimiter = new RateLimiter(capacity, tokenDelayMillis);
        spreadGetTokens(capacity, tokenDelayMillis / capacity);

        // When capacity is doubled on rate limiter
        rateLimiter.setParameters(capacity * 2, tokenDelayMillis);
        spreadGetTokens(capacity, tokenDelayMillis / capacity); // wait for effective rate to settle to new value
        final long elapsedMillis = timeGetTokens(32);

        // Then rate if retrieved tokens is doubled, after a delay
        final int minExpectedElapsedMillis = 164; // ideal - tokenDelayMillis - tolerance = 246 - 77 - 5
        final int maxExpectedElapsedMillis = 328; // ideal + tokenDelayMillis + tolerance = 246 + 77 + 5
        assertTrue("Minimum expected time elapsed to get tokens is " + minExpectedElapsedMillis + "ms but actual was "
                + elapsedMillis + "ms", elapsedMillis >= minExpectedElapsedMillis);
        assertTrue("Maximum expected time elapsed to get tokens is " + maxExpectedElapsedMillis + "ms but actual was "
                + elapsedMillis + "ms", elapsedMillis <= maxExpectedElapsedMillis);
    }

    @Test(timeout = 1000)
    public void shouldHalveRate_onHalfCapacity() throws Exception {
        // Given initial rate is 10 tokens per 77ms, which is 130 tokens per second
        final int capacity = 10;
        final long tokenDelayMillis = 77;
        rateLimiter = new RateLimiter(capacity, tokenDelayMillis);
        spreadGetTokens(capacity, tokenDelayMillis / capacity);

        // When capacity is halved on rate limiter
        rateLimiter.setParameters(capacity / 2, tokenDelayMillis);
        spreadGetTokens(capacity, tokenDelayMillis / capacity); // wait for effective rate to settle to new value
        final long elapsedMillis = timeGetTokens(16);

        // Then rate of retrieved tokens is halved, after a delay
        final int minExpectedElapsedMillis = 164; // ideal - tokenDelayMillis - tolerance = 246 - 77 - 5
        final int maxExpectedElapsedMillis = 328; // ideal + tokenDelayMillis + tolerance = 246 + 77 + 5
        assertTrue("Minimum expected time elapsed to get tokens is " + minExpectedElapsedMillis + "ms but actual was "
                + elapsedMillis + "ms", elapsedMillis >= minExpectedElapsedMillis);
        assertTrue("Maximum expected time elapsed to get tokens is " + maxExpectedElapsedMillis + "ms but actual was "
                + elapsedMillis + "ms", elapsedMillis <= maxExpectedElapsedMillis);
    }

    @Test(timeout = 1000)
    public void shouldDoubleRate_onHalfDelay() throws Exception {
        // Given initial rate is 3 tokens per 103ms, which is approximately 29 tokens per second
        final int capacity = 3;
        final int tokenDelayMillis = 103;
        rateLimiter = new RateLimiter(capacity, tokenDelayMillis);
        spreadGetTokens(capacity, tokenDelayMillis / capacity);

        // When delay is halved on rate limiter
        rateLimiter.setParameters(capacity, tokenDelayMillis / 2);
        spreadGetTokens(capacity, tokenDelayMillis / capacity); // wait for effective rate to settle to new value
        final long elapsedMillis = timeGetTokens(15);

        // Then rate of retrieved tokens is halved, after a delay
        final int minExpectedElapsedMillis = 198; // ideal - tokenDelayMillis - tolerance = 255 - 52 - 5
        final int maxExpectedElapsedMillis = 312; // ideal + tokenDelayMillis + tolerance = 255 + 52 + 5
        assertTrue("Minimum expected time elapsed to get tokens is " + minExpectedElapsedMillis + "ms but actual was "
                + elapsedMillis + "ms", elapsedMillis >= minExpectedElapsedMillis);
        assertTrue("Maximum expected time elapsed to get tokens is " + maxExpectedElapsedMillis + "ms but actual was "
                + elapsedMillis + "ms", elapsedMillis <= maxExpectedElapsedMillis);
    }

    @Test(timeout = 1000)
    public void shouldHalveRate_onDoubleDelay() throws Exception {
        // Given initial rate is 3 tokens per 31ms, which is approximately 98 tokens per second
        final int capacity = 3;
        final int tokenDelayMillis = 31;
        rateLimiter = new RateLimiter(capacity, tokenDelayMillis);
        spreadGetTokens(capacity, tokenDelayMillis / capacity);

        // When delay is doubled on rate limiter
        rateLimiter.setParameters(capacity, tokenDelayMillis * 2);
        spreadGetTokens(capacity, tokenDelayMillis / capacity); // wait for effective rate to settle to new value
        final long elapsedMillis = timeGetTokens(12);

        // Then rate of retrieved tokens is doubled, after a delay
        final int minExpectedElapsedMillis = 181; // ideal - tokenDelayMillis - tolerance = 248 - 62 - 5
        final int maxExpectedElapsedMillis = 315; // ideal + tokenDelayMillis + tolerance = 248 + 62 + 5
        assertTrue("Minimum expected time elapsed to get tokens is " + minExpectedElapsedMillis + "ms but actual was "
                + elapsedMillis + "ms", elapsedMillis >= minExpectedElapsedMillis);
        assertTrue("Maximum expected time elapsed to get tokens is " + maxExpectedElapsedMillis + "ms but actual was "
                + elapsedMillis + "ms", elapsedMillis <= maxExpectedElapsedMillis);
    }

    @Test(timeout = 1000)
    public void shouldReturnTokens_onGetTokensFromThreads() throws Exception {
        // Given
        final int capacity = 2;
        final int tokenDelayMillis = 50; // rate = 40 tokens per second
        rateLimiter = new RateLimiter(capacity, tokenDelayMillis);
        final int numThreads = 5;
        final int numTokensPerThread = 4;
        final long idealElapsedMillis = numThreads * numTokensPerThread * tokenDelayMillis / capacity; // = 500
        final long maxExpectedVarianceMillis = tokenDelayMillis * 3 / 2; // = 75
        final long minExpectedElapsedMillis = idealElapsedMillis - maxExpectedVarianceMillis; // = 425
        countDownLatch = new CountDownLatch(numThreads);
        timeGetTokens(capacity); // take all tokens

        // When
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < numThreads; i++) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        timeGetTokens(numTokensPerThread);
                        countDownLatch.countDown();
                    } catch (final Exception e) {
                        // Do nothing
                    }
                }
            }.start();
        }
        countDownLatch.await(); // wait for all threads to complete
        final long elapsedMillis = System.currentTimeMillis() - startTime;

        // Then
        assertTrue("Minimum expected time elapsed to get tokens is " + minExpectedElapsedMillis + "ms but actual was "
                + elapsedMillis + "ms", elapsedMillis >= minExpectedElapsedMillis);
    }

    @Test
    public void shouldReturnParameters_onGet() {
        // Given
        final int capacity = 1 + Randoms.randomInt(50);
        final long tokenDelayMillis = 10 + Randoms.randomInt(5000);
        rateLimiter = new RateLimiter(capacity, tokenDelayMillis);

        // When
        final int returnedCapacity = rateLimiter.getBucketCapacity();
        final long returnedTokenDelayMillis = rateLimiter.getTokenReplacementDelayMillis();

        // Then
        assertEquals(capacity, returnedCapacity);
        assertEquals(tokenDelayMillis, returnedTokenDelayMillis);
    }

    @Test
    public void shouldReturnUpdatedParameters_onGet() {
        // Given
        rateLimiter = new RateLimiter(10, 1000);
        final int capacity = 1 + Randoms.randomInt(50);
        final long tokenDelayMillis = 10 + Randoms.randomInt(5000);
        rateLimiter.setParameters(capacity, tokenDelayMillis);

        // When
        final int returnedCapacity = rateLimiter.getBucketCapacity();
        final long returnedTokenDelayMillis = rateLimiter.getTokenReplacementDelayMillis();

        // Then
        assertEquals(capacity, returnedCapacity);
        assertEquals(tokenDelayMillis, returnedTokenDelayMillis);
    }

    @Test
    public void shouldNotCreateRateLimiter_withZeroCapacity() {
        // Given
        final int capacity = 0;
        final long tokenDelayMillis = 10 + Randoms.randomInt(5000);

        // When
        IllegalArgumentException actualException = null;
        try {
            new RateLimiter(capacity, tokenDelayMillis);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotCreateRateLimiter_withNegativeDelay() {
        // Given
        final int capacity = 1 + Randoms.randomInt(50);
        final long tokenDelayMillis = -1 - Randoms.randomInt(5000);

        // When
        IllegalArgumentException actualException = null;
        try {
            new RateLimiter(capacity, tokenDelayMillis);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotSetParameters_withZeroCapacity() {
        // Given
        rateLimiter = new RateLimiter(10, 1000);
        final int capacity = 0;
        final long tokenDelayMillis = 10 + Randoms.randomInt(5000);

        // When
        IllegalArgumentException actualException = null;
        try {
            rateLimiter.setParameters(capacity, tokenDelayMillis);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldNotSetParameters_withNegativeDelay() {
        // Given
        rateLimiter = new RateLimiter(10, 1000);
        final int capacity = 1 + Randoms.randomInt(50);
        final long tokenDelayMillis = -1 - Randoms.randomInt(5000);

        // When
        IllegalArgumentException actualException = null;
        try {
            rateLimiter.setParameters(capacity, tokenDelayMillis);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    /**
     * Take a number of tokens and return the total time elapsed
     * @param numTokens Number of tokens to take
     * @return Total time elapsed, in milliseconds
     * @throws Exception
     */
    private long timeGetTokens(final int numTokens) throws Exception {
        final long startTime = System.currentTimeMillis();
        for (int i = 0; i < numTokens; i++) {
            rateLimiter.takeToken();
        }
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Take a number of tokens, with a delay after each one
     * @param numTokens Number of tokens to take
     * @param delayAfterGetToken Delay in milliseconds after getting each token
     * @throws Exception
     */
    private void spreadGetTokens(final int numTokens, final long delayAfterGetToken) throws Exception {
        for (int i = 0; i < numTokens; i++) {
            rateLimiter.takeToken();
            Thread.sleep(delayAfterGetToken);
        }
    }
}
