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

import static com.clicktravel.common.random.Randoms.randomId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.clicktravel.cheddar.request.context.SecurityContextHolder;

public class SecurityContextHolderTest {

    @Test
    public void shouldSetSecurityContext_withOneThread() {
        // Given
        final String principal = randomId();

        // When
        SecurityContextHolder.setPrincipal(principal);
        final String resultPrincipal = SecurityContextHolder.getPrincipal();

        // Then
        assertNotNull(resultPrincipal);
        assertEquals(principal, resultPrincipal);
    }

    @Test
    public void shouldSetSecurityContext_withTwoThreads() throws Exception {
        // Given
        final String principal1 = randomId();
        final String principal2 = randomId();

        // When
        final ContextTestThread thread1 = new ContextTestThread(principal1);
        final ContextTestThread thread2 = new ContextTestThread(principal2);
        thread1.start();
        thread2.start();
        thread2.carryOn();
        Thread.sleep(1000);
        thread1.carryOn();
        Thread.sleep(1000);
        final String resultPrincipal1 = thread1.getResultPrincipal();
        final String resultPrincipal2 = thread2.getResultPrincipal();

        // Then
        assertNotNull(resultPrincipal1);
        assertEquals(principal1, resultPrincipal1);
        assertNotNull(resultPrincipal2);
        assertEquals(principal2, resultPrincipal2);
    }

    private static class ContextTestThread extends Thread {

        private final String principal;
        private String resultPrincipal;
        private boolean notRunning = true;

        public ContextTestThread(final String principal) {
            this.principal = principal;
        }

        public void carryOn() {
            notRunning = false;
        }

        @Override
        public void run() {
            SecurityContextHolder.setPrincipal(principal);
            while (notRunning) {
                try {
                    // Give the other thread time to process
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                }
            }
            resultPrincipal = SecurityContextHolder.getPrincipal();
        }

        public String getResultPrincipal() {
            return resultPrincipal;
        }

    }

}
