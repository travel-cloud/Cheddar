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
package com.clicktravel.cheddar.application.security;

import static org.powermock.api.mockito.PowerMockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SecurityChecker.class)
public class TeamAspectTest {

    @Test
    public void shouldCheckForAnyTeamInSecurityContext_withMandatoryTeam() {
        // Given
        mockStatic(SecurityChecker.class);
        final TeamAspect aspect = new TeamAspect();
        final Team mockTeam = mock(Team.class);
        when(mockTeam.optional()).thenReturn(false);

        // When
        aspect.checkTeamInSecurityContext(mockTeam);

        // Then
        verifyStatic();
        SecurityChecker.checkAnyTeam();
    }

    @Test
    public void shouldNotCheckForAnyTeamInSecurityContext_withOptionalTeam() {
        // Given
        mockStatic(SecurityChecker.class);
        final TeamAspect aspect = new TeamAspect();
        final Team mockTeam = mock(Team.class);
        when(mockTeam.optional()).thenReturn(true);

        // When
        aspect.checkTeamInSecurityContext(mockTeam);

        // Then
        verifyStatic();
        verifyZeroInteractions(SecurityChecker.class);
    }
}
