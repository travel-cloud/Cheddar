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
package com.clicktravel.cheddar.infrastructure.messaging.pooled.listener;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.common.random.Randoms;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MessageHandlerWorkerThreadFactory.class)
public class MessageHandlerWorkerThreadFactoryTest {

    private MessageHandlerWorkerThreadFactory factory;
    private String queueName;

    @Before
    public void setUp() {
        queueName = Randoms.randomString();
        factory = new MessageHandlerWorkerThreadFactory(queueName);
    }

    @Test
    public void shouldCreateNewThreads() throws Exception {
        // Given
        final int numThreads = 5;
        final Runnable runnable = mock(Runnable.class);
        final List<Thread> expectedThreads = new LinkedList<>();
        for (int seq = 1; seq <= numThreads; seq++) {
            final Thread expectedThread = mock(Thread.class);
            expectedThreads.add(expectedThread);
            whenNew(Thread.class).withArguments(runnable, "MessageHandler:" + queueName + ":" + seq)
                    .thenReturn(expectedThread);
        }

        // When
        final List<Thread> actualThreads = new LinkedList<>();
        for (int n = 0; n < numThreads; n++) {
            actualThreads.add(factory.newThread(runnable));
        }

        // Then
        assertEquals(expectedThreads, actualThreads);
    }
}
