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
package com.clicktravel.cheddar.request.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class SecurityContextHolderTest {

    @Test
    public void shouldSetSecurityContext_withOneThread() {
        // Given
        final SecurityContext securityContext = mock(SecurityContext.class);

        // When
        SecurityContextHolder.set(securityContext);
        final SecurityContext resultSecurityContext = SecurityContextHolder.get();

        // Then
        assertNotNull(resultSecurityContext);
        assertEquals(securityContext, resultSecurityContext);
    }

    @Test
    public void shouldSetSecurityContext_withTwoThreads() throws Exception {
        // Given
        final SecurityContext securityContext1 = mock(SecurityContext.class);
        final SecurityContext securityContext2 = mock(SecurityContext.class);

        // When
        final ContextTestThread thread1 = new ContextTestThread(securityContext1);
        final ContextTestThread thread2 = new ContextTestThread(securityContext2);
        thread1.start();
        thread2.start();
        thread2.carryOn();
        Thread.sleep(1000);
        thread1.carryOn();
        Thread.sleep(1000);
        final SecurityContext resultSecurityContext1 = thread1.getResultSecurityContext();
        final SecurityContext resultSecurityContext2 = thread2.getResultSecurityContext();

        // Then
        assertNotNull(resultSecurityContext1);
        assertEquals(securityContext1, resultSecurityContext1);
        assertNotNull(resultSecurityContext2);
        assertEquals(securityContext2, resultSecurityContext2);
    }

    private static class ContextTestThread extends Thread {

        private final SecurityContext securityContext;
        private SecurityContext resultSecurityContext;
        private boolean notRunning = true;

        public ContextTestThread(final SecurityContext securityContext) {
            this.securityContext = securityContext;
        }

        public void carryOn() {
            notRunning = false;
        }

        @Override
        public void run() {
            SecurityContextHolder.set(securityContext);
            while (notRunning) {
                try {
                    // Give the other thread time to process
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                }
            }
            resultSecurityContext = SecurityContextHolder.get();
        }

        public SecurityContext getResultSecurityContext() {
            return resultSecurityContext;
        }

    }

}
