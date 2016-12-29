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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.request.context.SecurityContext;
import com.clicktravel.cheddar.request.context.SecurityContextHolder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SecurityContextHolder.class })
public class SecurityCheckerTest {

    @Test
    public void shouldPassCheckPrincipalCheck_withMatchingUserId() {
        // Given
        final String principal = randomId();
        final SecurityContext mockSecurityContext = mock(SecurityContext.class);
        when(mockSecurityContext.userId()).thenReturn(Optional.of(principal));
        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.get()).thenReturn(mockSecurityContext);

        // When
        CredentialsMissingException thrownException = null;
        try {
            SecurityChecker.checkUser(principal);
        } catch (final CredentialsMissingException e) {
            thrownException = e;
        }

        // Then
        assertNull(thrownException);
    }

    @Test
    public void shouldNotPassCheckPrincipalCheck_withNoUserInSecurityContext() {
        // Given
        final String principal = randomId();
        final SecurityContext mockSecurityContext = mock(SecurityContext.class);
        when(mockSecurityContext.userId()).thenReturn(Optional.empty());
        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.get()).thenReturn(mockSecurityContext);

        // When
        CredentialsMissingException thrownException = null;
        try {
            SecurityChecker.checkUser(principal);
        } catch (final CredentialsMissingException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldNotPassCheckPrincipalCheck_withDifferentUserId() {
        // Given
        final String principal = randomId();
        final SecurityContext mockSecurityContext = mock(SecurityContext.class);
        when(mockSecurityContext.userId()).thenReturn(Optional.of(randomId()));
        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.get()).thenReturn(mockSecurityContext);

        // When
        SecurityConstraintViolationException thrownException = null;
        try {
            SecurityChecker.checkUser(principal);
        } catch (final SecurityConstraintViolationException e) {
            thrownException = e;
        }

        // Then
        assertNotNull(thrownException);
    }

    @Test
    public void shouldPassAutheticatedCheck_withUserInRquestContext() {
        // Given
        final SecurityContext mockSecurityContext = mock(SecurityContext.class);
        when(mockSecurityContext.userId()).thenReturn(Optional.of(randomId()));
        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.get()).thenReturn(mockSecurityContext);

        // When
        CredentialsMissingException thrownException = null;
        try {
            SecurityChecker.checkAnyUser();
        } catch (final CredentialsMissingException e) {
            thrownException = e;
        }

        // Then
        assertNull(thrownException);
    }

    @Test
    public void shouldNotPassAutheticatedCheck_withNoUserInSecurityContext() {
        // Given
        final SecurityContext mockSecurityContext = mock(SecurityContext.class);
        when(mockSecurityContext.userId()).thenReturn(Optional.empty());
        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.get()).thenReturn(mockSecurityContext);

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
}
