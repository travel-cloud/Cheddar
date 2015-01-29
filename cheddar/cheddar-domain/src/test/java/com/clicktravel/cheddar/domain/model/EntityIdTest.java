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
package com.clicktravel.cheddar.domain.model;

import static com.clicktravel.common.random.Randoms.randomId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.clicktravel.common.validation.ValidationException;

public class EntityIdTest {

    private static int RANDOM_SAMPLE_SIZE = 20;

    @Test
    public void shouldCreateEntityId_withId() {
        // Given
        final String id = randomId();

        // When
        final EntityIdStub entityId = new EntityIdStub(id);

        // Then
        assertEquals(id, entityId.id());
    }

    @Test
    public void shouldCreateEntityIdWithUniqueNonEmptyId() {
        // Given
        final Set<String> idSet = new HashSet<>();

        // When
        for (int n = 0; n < RANDOM_SAMPLE_SIZE; n++) {
            final EntityIdStub entityIdStub = new EntityIdStub();
            final String id = entityIdStub.id();
            assertNotNull(id);
            assertFalse(id.trim().isEmpty());
            idSet.add(id);
        }

        // Then
        assertEquals(RANDOM_SAMPLE_SIZE, idSet.size());
    }

    @Test
    public void shouldThrowValidationException_withNullId() {
        // Given
        final String id = null;

        testForValidationExceptionOnConstructionWithId(id);
    }

    @Test
    public void shouldThrowValidationException_withEmptyId() {
        // Given
        final String id = "";

        testForValidationExceptionOnConstructionWithId(id);
    }

    @Test
    public void shouldThrowValidationException_withWhitespaceId() {
        // Given
        final String id = " \t";

        testForValidationExceptionOnConstructionWithId(id);
    }

    private void testForValidationExceptionOnConstructionWithId(final String id) {
        // When
        ValidationException actualException = null;
        try {
            new EntityIdStub(id);
        } catch (final ValidationException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertEquals("id", actualException.getFields()[0]);
    }

    @Test
    public void shouldBeEqualsAndHashEquals_givenSameId() {
        // Given
        final String id = randomId();
        final EntityIdStub entity1 = new EntityIdStub(id);
        final EntityIdStub entity2 = new EntityIdStub(id);

        // When
        final boolean isEquals = entity1.equals(entity2);
        final int entity1HashCode = entity1.hashCode();
        final int entity2HashCode = entity2.hashCode();

        // Then
        assertTrue(isEquals);
        assertTrue(entity1HashCode == entity2HashCode);
    }

    @Test
    public void shouldBeEquals_withSelf() {
        // Given
        final String id = randomId();
        final EntityIdStub entity = new EntityIdStub(id);

        // When
        final boolean isEquals = entity.equals(entity);

        // Then
        assertTrue(isEquals);
    }

    @Test
    public void shouldNotBeEquals_withNull() {
        // Given
        final EntityIdStub entityIdStub = new EntityIdStub();
        final EntityIdStub other = null;

        // When
        final boolean isEquals = entityIdStub.equals(other);

        // Then
        assertFalse(isEquals);
    }

    @Test
    public void shouldNotBeEquals_withDifferentClassInstance() {
        // Given
        final EntityIdStub entityIdStub = new EntityIdStub();
        final Object other = new Object();

        // When
        final boolean isEquals = entityIdStub.equals(other);

        // Then
        assertFalse(isEquals);
    }

    @Test
    public void shouldNotBeEquals_withDifferentId() {
        // Given
        final String id1 = randomId();
        final String id2 = randomId();
        final EntityIdStub entity1 = new EntityIdStub(id1);
        final EntityIdStub entity2 = new EntityIdStub(id2);

        // When
        final boolean isEquals = entity1.equals(entity2);

        // Then
        assertFalse(isEquals);
    }

    public static class EntityIdStub extends EntityId {

        public EntityIdStub() {
            super();
        }

        public EntityIdStub(final String id) {
            super(id);
        }

    }

}
