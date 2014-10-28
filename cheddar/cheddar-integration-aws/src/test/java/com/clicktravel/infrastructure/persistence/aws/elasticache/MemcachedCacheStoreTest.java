package com.clicktravel.infrastructure.persistence.aws.elasticache;

import static org.junit.Assert.assertEquals;
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

public class MemcachedCacheStoreTest {

    private final MemcachedClient client = mock(MemcachedClient.class);
    private MemcachedCacheStore memcachedCacheStore;

    @Before
    public void before() {
        memcachedCacheStore = new MemcachedCacheStore(client);
    }

    @Test
    public void shoudlGetObject() throws InterruptedException, TimeoutException, ExecutionException {

        final String key = Randoms.randomString();
        final String object = Randoms.randomString();
        final int timeout = Randoms.randomInt(5) + 1;

        final GetFuture<Object> f = mock(GetFuture.class);
        when(client.asyncGet(key)).thenReturn(f);
        when(f.get(timeout, TimeUnit.SECONDS)).thenReturn(object);
        final Object obj = memcachedCacheStore.getObject(key, timeout);

        assertEquals(object, obj);

    }

    @Test
    public void shoudlNotGetObject_throwsTimout() throws InterruptedException, ExecutionException, TimeoutException {

        final String key = Randoms.randomString();
        final int timeout = Randoms.randomInt(5) + 1;
        final GetFuture<Object> f = mock(GetFuture.class);
        when(client.asyncGet(key)).thenReturn(f);
        when(f.get(timeout, TimeUnit.SECONDS)).thenThrow(TimeoutException.class);
        final Object obj = memcachedCacheStore.getObject(key, timeout);
        assertNull(obj);
    }

    @Test
    public void shouldSetObject() {
        final String key = Randoms.randomString();
        final String object = Randoms.randomString();
        final int expire = Randoms.randomInt(5) + 1;
        memcachedCacheStore.setObject(key, expire, object);
        verify(client).set(key, expire, object);
    }
}
