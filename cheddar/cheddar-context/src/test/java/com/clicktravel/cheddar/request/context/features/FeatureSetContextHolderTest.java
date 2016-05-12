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
package com.clicktravel.cheddar.request.context.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class FeatureSetContextHolderTest {

    @Test
    public void shouldSetFeatureSetContext_withOneThread() {
        // Given
        final FeatureSetContext featureSetContext = mock(FeatureSetContext.class);

        // When
        FeatureSetContextHolder.set(featureSetContext);
        final FeatureSetContext resultFeatureSetContext = FeatureSetContextHolder.get();

        // Then
        assertNotNull(resultFeatureSetContext);
        assertEquals(featureSetContext, resultFeatureSetContext);
    }

    @Test
    public void shouldSetFeatureSetContext_withTwoThreads() throws Exception {
        // Given
        final FeatureSetContext featureSetContext1 = mock(FeatureSetContext.class);
        final FeatureSetContext featureSetContext2 = mock(FeatureSetContext.class);

        // When
        final ContextTestThread thread1 = new ContextTestThread(featureSetContext1);
        final ContextTestThread thread2 = new ContextTestThread(featureSetContext2);
        thread1.start();
        thread2.start();
        thread2.carryOn();
        Thread.sleep(1000);
        thread1.carryOn();
        Thread.sleep(1000);
        final FeatureSetContext resultFeatureSetContext1 = thread1.getResultFeatureSetContext();
        final FeatureSetContext resultFeatureSetContext2 = thread2.getResultFeatureSetContext();

        // Then
        assertNotNull(resultFeatureSetContext1);
        assertEquals(featureSetContext1, resultFeatureSetContext1);
        assertNotNull(resultFeatureSetContext2);
        assertEquals(featureSetContext2, resultFeatureSetContext2);
    }

    private static class ContextTestThread extends Thread {

        private final FeatureSetContext featureSetContext;
        private FeatureSetContext resultFeatureSetContext;
        private boolean notRunning = true;

        public ContextTestThread(final FeatureSetContext featureSetContext) {
            this.featureSetContext = featureSetContext;
        }

        public void carryOn() {
            notRunning = false;
        }

        @Override
        public void run() {
            FeatureSetContextHolder.set(featureSetContext);
            while (notRunning) {
                try {
                    // Give the other thread time to process
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                }
            }
            resultFeatureSetContext = FeatureSetContextHolder.get();
        }

        public FeatureSetContext getResultFeatureSetContext() {
            return resultFeatureSetContext;
        }

    }

}
