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

import java.util.NoSuchElementException;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * A concurrent blocking rate limiter based on the 'token bucket' algorithm. A rate limiter is used to control the rate
 * at which some (possibly shared) resource is accessed, or unit of work is executed. Before the resource is accessed or
 * work unit executed, a token (permit) must first be obtained from the rate limiter; this operation blocks until a
 * token is available. A bucket of tokens is maintained by the rate limiter, and each token taken from the bucket is
 * automatically replaced after a fixed delay. The bucket has a fixed capacity and is initialised to be full of tokens.
 * <p/>
 * If the rate limiter is configured to have capacity of <code>C</code> tokens and a token delay of <code>D</code>
 * milliseconds, then
 * <ul>
 * <li>The maximum rate R of tokens (measured over a long period of time) that may be obtained is given by
 * <code>R = (1000 * C / D)</code> tokens per second</li>
 * <li>The maximum number of tokens that may be obtained simultaneously ('bursty' behaviour) is <code>C</code> tokens</li>
 * </ul>
 * <p/>
 * Assuming tokens are obtained as soon as they become available at a steady rate, it takes time D to obtain C tokens.
 * If the tokens are used as work item permits, up to D milliseconds of work may be outstanding at any instant in time.
 * To limit the amount of outstanding work, it is useful to fix D and set C = D * R / 1000. Examples:
 * <ul>
 * <li>With fixed D = 5000 ms: For rate R = 10 tokens/second, set C = 50; for rate R = 20 tokens/second, set C = 100</li>
 * <li>With fixed D = 2000 ms: For rate R = 10 tokens/second, set C = 20; for rate R = 20 tokens/second, set C = 40</li>
 * <li>With fixed D = 1000 ms: For rate R = 10 tokens/second, set C = 10; for rate R = 20 tokens/second, set C = 20</li>
 * </ul>
 * <p/>
 * The parameters (capacity & token delay) are set on construction of the rate limiter. The parameters may be changed
 * after construction, though there may be a transition period where the effective rate limitation adjusts to the new
 * parameter values.
 */
public class RateLimiter {

    /**
     * Bucket of tokens. The bucket is maintained to contain a number of tokens equal to capacity (the bucket is always
     * full), however each token may or may not be available to be taken. Tokens implement {@link Delayed}, where all
     * available tokens have expired delay.
     */
    private final DelayQueue<Token> bucket = new DelayQueue<>();

    /** Number of tokens to maintain in bucket */
    private int bucketCapacity;

    /** Delay value for replacement tokens, in milliseconds */
    private volatile long tokenReplacementDelayMillis;

    /**
     * Constructs a rate limiter based on the 'token bucket' algorithm. Maximum allowed rate is
     * <code>1000 * bucketCapacity / tokenReplacementDelayMillis</code> tokens per second. Maximum number of tokens that
     * may be obtained simultaneously is <code>bucketCapacity</code>
     * @param bucketCapacity Maximum number of tokens in bucket
     * @param tokenReplacementDelayMillis Delay before a token is replaced, in milliseconds
     */
    public RateLimiter(final int bucketCapacity, final long tokenReplacementDelayMillis) {
        setParameters(bucketCapacity, tokenReplacementDelayMillis);
    }

    /**
     * Sets the rate limiter parameters to new values. Rate limitation to the new parameter values will not be
     * completely effective for a certain period. This transition period will be anything up to the previous value of
     * tokenReplacementDelayMillis in length.
     * @param bucketCapacity New Maximum number of tokens in bucket. If the bucket currently has less capacity, new
     *            tokens are added which are immediately available.
     * @param tokenReplacementDelayMillis New delay before a token is replaced, in milliseconds
     */
    public synchronized void setParameters(final int bucketCapacity, final long tokenReplacementDelayMillis) {
        setTokenReplacementDelayMillis(tokenReplacementDelayMillis);
        setBucketCapacity(bucketCapacity);
        restoreBucket(0L);
    }

    /**
     * Obtain a rate limited token, blocking if necessary until one is available.
     * @throws InterruptedException
     */
    public void takeToken() throws InterruptedException {
        processObtainedToken(bucket.take()); // block until token is available
    }

    /**
     * Obtains a rate limited token, but only if such a token is immediately available.
     * @return <code>true</code> if a token was obtained
     */
    public boolean pollToken() {
        return processObtainedToken(bucket.poll()); // no blocking
    }

    /**
     * Obtain a rate limited token, blocking if necessary until one is available or timeout occurs.
     * @param timeout How long to wait for obtaining a token, in units of <code>unit</code>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the <code>timeout</code> parameter
     * @return <code>true</code> if a token was obtained, <code>false</code> if timeout occurs
     * @throws InterruptedException
     */
    public boolean pollToken(final long timeout, final TimeUnit unit) throws InterruptedException {
        return processObtainedToken(bucket.poll(timeout, unit)); // block until token available or timeout
    }

    private boolean processObtainedToken(final Token token) {
        if (token == null) {
            return false;
        } else {
            restoreBucket(tokenReplacementDelayMillis);
            return true;
        }
    }

    /**
     * Restore the total number of tokens (both expired and not expired) in the bucket.
     * @param delayMillis Availability delay given to any tokens added to the bucket
     */
    private synchronized void restoreBucket(final long delayMillis) {
        int diff;
        while ((diff = bucket.size() - bucketCapacity) != 0) {
            if (diff > 0) {
                try {
                    bucket.remove();
                } catch (final NoSuchElementException e) {
                    // Do nothing
                    // takeToken() or pollToken() emptied the bucket first
                }
            } else {
                bucket.add(new Token(delayMillis)); // replace token, but delay availability
            }
        }
    }

    public int getBucketCapacity() {
        return bucketCapacity;
    }

    public long getTokenReplacementDelayMillis() {
        return tokenReplacementDelayMillis;
    }

    private void setBucketCapacity(final int bucketCapacity) {
        if (bucketCapacity < 1) {
            throw new IllegalArgumentException("Bucket must have capacity of at least 1 token");
        }
        this.bucketCapacity = bucketCapacity;
    }

    private void setTokenReplacementDelayMillis(final long tokenReplacementDelayMillis) {
        if (tokenReplacementDelayMillis < 0) {
            throw new IllegalArgumentException("Token replacement delay cannot be negative");
        }
        this.tokenReplacementDelayMillis = tokenReplacementDelayMillis;
    }

    private class Token implements Delayed {

        final long endTime;

        Token(final long totalDelayMillis) {
            endTime = System.currentTimeMillis() + totalDelayMillis;
        }

        @Override
        public int compareTo(final Delayed o) {
            final long diffMillis = endTime - ((Token) o).endTime;
            return diffMillis == 0 ? 0 : (diffMillis < 0 ? -1 : 1);
        }

        @Override
        public long getDelay(final TimeUnit unit) {
            final long remainingDelayMillis = endTime - System.currentTimeMillis(); // <= 0 if delay has expired
            return unit.convert(remainingDelayMillis, TimeUnit.MILLISECONDS);
        }
    }
}
