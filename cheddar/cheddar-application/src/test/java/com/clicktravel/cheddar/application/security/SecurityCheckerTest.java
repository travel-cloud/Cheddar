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

import static com.clicktravel.common.random.Randoms.randomId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.request.context.SecurityContext;
import com.clicktravel.cheddar.request.context.SecurityContextHolder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SecurityContextHolder.class })
public class SecurityCheckerTest {

    private SecurityContext mockSecurityContext;

    @Before
    public void setUp() {
        mockSecurityContext = mock(SecurityContext.class);
        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.get()).thenReturn(mockSecurityContext);
    }

    @Test
    public void shouldPassUserCheck_withMatchingUserIdInSecurityContext() {
        // Given
        final String userId = randomId();
        when(mockSecurityContext.userId()).thenReturn(Optional.of(userId));

        // When
        Exception thrownException = null;
        try {
            SecurityChecker.checkUser(userId);
        } catch (final Exception e) {
            thrownException = e;
        }

        // Then
        assertNull(thrownException);
    }

    @Test
    public void shouldNotPassUserCheck_withNoUserIdInSecurityContext() {
        // Given
        final String userId = randomId();
        when(mockSecurityContext.userId()).thenReturn(Optional.empty());

        // When
        CredentialsMissingException thrownException = null;
        try {
            SecurityChecker.checkUser(userId);
        } catch (final CredentialsMissingException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldNotPassUserCheck_withDifferentUserIdInSecurityContext() {
        // Given
        final String userId = randomId();
        when(mockSecurityContext.userId()).thenReturn(Optional.of(randomId()));

        // When
        SecurityConstraintViolationException thrownException = null;
        try {
            SecurityChecker.checkUser(userId);
        } catch (final SecurityConstraintViolationException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldPassAnyUserCheck_withUserIdInSecurityContext() {
        // Given
        when(mockSecurityContext.userId()).thenReturn(Optional.of(randomId()));

        // When
        Exception thrownException = null;
        try {
            SecurityChecker.checkAnyUser();
        } catch (final Exception e) {
            thrownException = e;
        }

        // Then
        assertNull(thrownException);
    }

    @Test
    public void shouldNotPassAnyUserCheck_withNoUserIdInSecurityContext() {
        // Given
        when(mockSecurityContext.userId()).thenReturn(Optional.empty());

        // When
        CredentialsMissingException thrownException = null;
        try {
            SecurityChecker.checkAnyUser();
        } catch (final CredentialsMissingException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldPassTeamCheck_withMatchingTeamIdInSecurityContext() {
        // Given
        final String teamId = randomId();
        when(mockSecurityContext.teamId()).thenReturn(Optional.of(teamId));

        // When
        Exception thrownException = null;
        try {
            SecurityChecker.checkTeam(teamId);
        } catch (final Exception e) {
            thrownException = e;
        }

        // Then
        assertNull(thrownException);
    }

    @Test
    public void shouldNotPassTeamCheck_withNoTeamIdInSecurityContext() {
        // Given
        final String teamId = randomId();
        when(mockSecurityContext.teamId()).thenReturn(Optional.empty());

        // When
        CredentialsMissingException thrownException = null;
        try {
            SecurityChecker.checkTeam(teamId);
        } catch (final CredentialsMissingException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldNotPassTeamCheck_withDifferentTeamIdInSecurityContext() {
        // Given
        final String teamId = randomId();
        when(mockSecurityContext.teamId()).thenReturn(Optional.of(randomId()));

        // When
        SecurityConstraintViolationException thrownException = null;
        try {
            SecurityChecker.checkTeam(teamId);
        } catch (final SecurityConstraintViolationException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldPassAnyTeamCheck_withTeamIdInSecurityContext() {
        // Given
        when(mockSecurityContext.teamId()).thenReturn(Optional.of(randomId()));

        // When
        Exception thrownException = null;
        try {
            SecurityChecker.checkAnyTeam();
        } catch (final Exception e) {
            thrownException = e;
        }

        // Then
        assertNull(thrownException);
    }

    @Test
    public void shouldNotPassAnyTeamCheck_withNoTeamIdInSecurityContext() {
        // Given
        when(mockSecurityContext.teamId()).thenReturn(Optional.empty());

        // When
        CredentialsMissingException thrownException = null;
        try {
            SecurityChecker.checkAnyTeam();
        } catch (final CredentialsMissingException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }
}
