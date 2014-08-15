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

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTimeUtils;

/**
 * {@link BlockingQueue} decorator that limits the rate that items may be taken from the queue. The methods
 * {@link #take()}, {@link #poll()} and {@link #poll(long, TimeUnit)} are subject to rate limitation.
 * @param <E> The type of elements held on the queue
 */
public class RateLimitedQueue<E> implements BlockingQueue<E> {

    private final BlockingQueue<E> queue;
    private final RateLimiter rateLimiter;

    /**
     * Decorates a {@link BlockingQueue} with a {@link RateLimiter} that limits rate of items taken from queue.
     * @param queue BlockingQueue to decorate
     * @param rateLimiter RateLimiter used to limit rate of items taken from queue
     */
    public RateLimitedQueue(final BlockingQueue<E> queue, final RateLimiter rateLimiter) {
        this.queue = queue;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public E take() throws InterruptedException {
        rateLimiter.takeToken(); // block until rate limited token is obtained
        return queue.take();
    }

    @Override
    public E poll() {
        return rateLimiter.pollToken() ? queue.poll() : null;
    }

    @Override
    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        final long startTime = DateTimeUtils.currentTimeMillis();
        if (!rateLimiter.pollToken(timeout, unit)) {
            return null; // timeout waiting for token
        }

        // Poll queue with timeout set to time remaining
        final long timeoutNanos = unit.toNanos(timeout);
        final long elapsedMillis = DateTimeUtils.currentTimeMillis() - startTime;
        final long elapsedNanos = TimeUnit.MILLISECONDS.toNanos(elapsedMillis);
        final long remainingNanos = timeoutNanos - elapsedNanos;
        return (remainingNanos > 0) ? queue.poll(remainingNanos, TimeUnit.NANOSECONDS) : null;
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    @Override
    public boolean add(final E e) {
        return queue.add(e);
    }

    @Override
    public Iterator<E> iterator() {
        return queue.iterator();
    }

    @Override
    public E remove() {
        return queue.remove();
    }

    @Override
    public boolean offer(final E e) {
        return queue.offer(e);
    }

    @Override
    public Object[] toArray() {
        return queue.toArray();
    }

    @Override
    public E element() {
        return queue.element();
    }

    @Override
    public E peek() {
        return queue.peek();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return queue.toArray(a);
    }

    @Override
    public void put(final E e) throws InterruptedException {
        queue.put(e);
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        return queue.offer(e, timeout, unit);
    }

    @Override
    public int remainingCapacity() {
        return queue.remainingCapacity();
    }

    @Override
    public boolean remove(final Object o) {
        return queue.remove(o);
    }

    @Override
    public boolean contains(final Object o) {
        return queue.contains(o);
    }

    @Override
    public int drainTo(final Collection<? super E> c) {
        return queue.drainTo(c);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return queue.containsAll(c);
    }

    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        return queue.drainTo(c, maxElements);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        return queue.addAll(c);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return queue.removeAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return queue.retainAll(c);
    }

    @Override
    public void clear() {
        queue.clear();
    }
}
