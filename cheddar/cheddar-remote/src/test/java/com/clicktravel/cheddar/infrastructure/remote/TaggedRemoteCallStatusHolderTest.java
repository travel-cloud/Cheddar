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
package com.clicktravel.cheddar.infrastructure.remote;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.remote.TaggedRemoteCallStatusHolderImpl;

public class TaggedRemoteCallStatusHolderTest {

    private TaggedRemoteCallStatusHolderImpl holder;

    @Before
    public void setUp() {
        holder = new TaggedRemoteCallStatusHolderImpl();
    }

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void shouldReturnFalse_onNoRemoteCalls() {
        // When
        final boolean processedRecentTaggedRemoteCall = holder.processedRecentTaggedRemoteCall();

        // Then
        assertFalse(processedRecentTaggedRemoteCall);
    }

    @Test
    public void shouldReturnTrue_duringRemoteCall() {
        // Given
        holder.taggedRemoteCallStarted();

        // When
        final boolean processedRecentTaggedRemoteCall = holder.processedRecentTaggedRemoteCall();

        // Then
        assertTrue(processedRecentTaggedRemoteCall);
    }

    @Test
    public void shouldReturnTrue_shortlyAfterRemoteCall() {
        // Given
        holder.taggedRemoteCallStarted();
        holder.taggedRemoteCallCompleted();

        // When
        final boolean processedRecentTaggedRemoteCall = holder.processedRecentTaggedRemoteCall();

        // Then
        assertTrue(processedRecentTaggedRemoteCall);
    }

    @Test
    public void shouldReturnFalse_over30SecondsAfterRemoteCall() {
        // Given
        holder.taggedRemoteCallStarted();
        holder.taggedRemoteCallCompleted();
        DateTimeUtils.setCurrentMillisOffset(31 * 1000); // simulate 31 second wait

        // When
        final boolean processedRecentTaggedRemoteCall = holder.processedRecentTaggedRemoteCall();

        // Then
        assertFalse(processedRecentTaggedRemoteCall);
    }
}
