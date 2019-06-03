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
package com.clicktravel.cheddar.features;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import org.junit.Test;

public class FeatureSetContextHolderTest {

    @Test
    public void shouldSetFeatureSetContext_withOneThread() {
        // Given
        final FeaturesContext featuresContext = mock(FeaturesContext.class);

        // When
        FeaturesContextHolder.set(featuresContext);
        final FeaturesContext resultFeatureSetContext = FeaturesContextHolder.get();

        // Then
        assertNotNull(resultFeatureSetContext);
        assertEquals(featuresContext, resultFeatureSetContext);
    }

    @Test
    public void shouldSetFeatureSetContext_withTwoThreads() throws Exception {
        // Given
        final FeaturesContext featureSetContext1 = mock(FeaturesContext.class);
        final FeaturesContext featureSetContext2 = mock(FeaturesContext.class);

        // When
        final ContextTestThread thread1 = new ContextTestThread(featureSetContext1);
        final ContextTestThread thread2 = new ContextTestThread(featureSetContext2);
        thread1.start();
        thread2.start();
        thread2.carryOn();
        Thread.sleep(1000);
        thread1.carryOn();
        Thread.sleep(1000);
        final FeaturesContext resultFeatureSetContext1 = thread1.getResultFeatureSetContext();
        final FeaturesContext resultFeatureSetContext2 = thread2.getResultFeatureSetContext();

        // Then
        assertNotNull(resultFeatureSetContext1);
        assertEquals(featureSetContext1, resultFeatureSetContext1);
        assertNotNull(resultFeatureSetContext2);
        assertEquals(featureSetContext2, resultFeatureSetContext2);
    }

    private static class ContextTestThread extends Thread {

        private final FeaturesContext featuresContext;
        private FeaturesContext resultFeatureSetContext;
        private boolean notRunning = true;

        public ContextTestThread(final FeaturesContext featuresContext) {
            this.featuresContext = featuresContext;
        }

        public void carryOn() {
            notRunning = false;
        }

        @Override
        public void run() {
            FeaturesContextHolder.set(featuresContext);
            while (notRunning) {
                try {
                    // Give the other thread time to process
                    Thread.sleep(100);
                } catch (final InterruptedException e) {
                }
            }
            resultFeatureSetContext = FeaturesContextHolder.get();
        }

        public FeaturesContext getResultFeatureSetContext() {
            return resultFeatureSetContext;
        }

    }

}
