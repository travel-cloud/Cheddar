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
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.request.context.SecurityContextHolder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SecurityContextHolder.class })
public class SecurityCheckerTest {

    @Test
    public void shouldNotPassPrincipalCheck_withNullCheckPrincipal() {
        // Given
        final String principal = randomId();
        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.getPrincipal()).thenReturn(principal);

        // When
        SecurityConstraintViolationException expectedException = null;
        try {
            SecurityChecker.checkPrincipal(null);
        } catch (final SecurityConstraintViolationException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }

    @Test
    public void shouldNotPassPrincipalCheck_withNullStoredPrincipal() {
        // Given
        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.getPrincipal()).thenReturn(null);
        final String principal = randomId();

        // When
        CredentialsMissingException expectedException = null;
        try {
            SecurityChecker.checkPrincipal(principal);
        } catch (final CredentialsMissingException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }

    @Test
    public void shouldPassAutheticatedCheck_withPrincipal() {
        // Given
        final String principal = randomId();
        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.getPrincipal()).thenReturn(principal);

        // When
        CredentialsMissingException unexpectedException = null;
        try {
            SecurityChecker.checkAuthenticated();
        } catch (final CredentialsMissingException e) {
            unexpectedException = e;
        }

        // Then
        assertNull(unexpectedException);
    }

    @Test
    public void shouldNotPassAutheticatedCheck_withNoPrincipal() {
        // Given
        mockStatic(SecurityContextHolder.class);
        when(SecurityContextHolder.getPrincipal()).thenReturn(null);

        // When
        CredentialsMissingException expectedException = null;
        try {
            SecurityChecker.checkAuthenticated();
        } catch (final CredentialsMissingException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }

}
