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
package com.clicktravel.infrastructure.persistence.aws.elasticache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.GetFuture;

import org.junit.Before;
import org.junit.Test;

import com.clicktravel.common.random.Randoms;

public class MemcachedItemCacheTest {

    private final MemcachedClient memcachedClient = mock(MemcachedClient.class);
    private MemcachedItemCache memcachedItemCache;

    @Before
    public void before() {
        memcachedItemCache = new MemcachedItemCache(memcachedClient);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldGetItem() throws InterruptedException, TimeoutException, ExecutionException {
        // Given
        final String key = Randoms.randomString();
        final String item = Randoms.randomString();
        final int timeout = Randoms.randomInt(5) + 1;
        final GetFuture<Object> f = mock(GetFuture.class);
        when(memcachedClient.asyncGet(key)).thenReturn(f);
        when(f.get(timeout, TimeUnit.SECONDS)).thenReturn(item);

        // When
        final Object obj = memcachedItemCache.getItem(key, timeout);

        // Then
        assertEquals(item, obj);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldNotGetItem_throwsTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        // Given
        final String key = Randoms.randomString();
        final int timeout = Randoms.randomInt(5) + 1;
        final GetFuture<Object> f = mock(GetFuture.class);
        when(memcachedClient.asyncGet(key)).thenReturn(f);
        when(f.get(timeout, TimeUnit.SECONDS)).thenThrow(TimeoutException.class);

        // When
        final Object item = memcachedItemCache.getItem(key, timeout);

        // Then
        assertNull(item);
    }

    @Test
    public void shouldPutItem() {
        // Given
        final String key = Randoms.randomString();
        final String item = Randoms.randomString();
        final long expire = Randoms.randomInt(5) + 1;

        // When
        memcachedItemCache.putItem(key, item, expire);

        // Then
        verify(memcachedClient).set(key, (int) expire, item);
    }

    @Test
    public void shouldNotPutItem_failExpireToLarge() {
        // Given
        final String key = Randoms.randomString();
        final String item = Randoms.randomString();
        final long expire = (long) Integer.MAX_VALUE + 1;

        // When
        IllegalArgumentException actualException = null;
        try {
            memcachedItemCache.putItem(key, item, expire);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }
}
