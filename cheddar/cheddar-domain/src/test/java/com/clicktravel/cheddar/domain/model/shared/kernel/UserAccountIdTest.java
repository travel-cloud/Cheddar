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
package com.clicktravel.cheddar.domain.model.shared.kernel;

import static com.clicktravel.common.random.Randoms.randomId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.Test;

public class UserAccountIdTest {

    @Test
    public void shouldCreateUserAccountId_withNoId() {
        // Given

        // When
        final UserAccountId userAccountId = new UserAccountId();

        // Then
        assertNotNull(userAccountId);
        assertNotNull(userAccountId.id());
        assertNotNull(UUID.fromString(userAccountId.id()));
    }

    @Test
    public void shouldCreateUserAccountId_withId() {
        // Given
        final String id = randomId();

        // When
        final UserAccountId userAccountId = new UserAccountId(id);

        // Then
        assertNotNull(userAccountId);
        assertNotNull(userAccountId.id());
        assertEquals(id, userAccountId.id());
    }

    @Test
    public void shouldNotReturnId_withNullUserAccountId() {
        // Given
        final UserAccountId userAccountId = UserAccountId.NULL;

        // When
        UnsupportedOperationException expectedException = null;
        try {
            userAccountId.id();
        } catch (final UnsupportedOperationException e) {
            expectedException = e;
        }

        // Then
        assertNotNull(expectedException);
    }

}
