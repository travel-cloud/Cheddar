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
package com.clicktravel.cheddar.infrastructure.persistence.database.tx;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.database.DatabaseTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.ItemConstraintViolationException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.OptimisticLockException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceException;

@Ignore
public class CreateActionTest {

    @Test
    public void shouldApply_withDatabaseTemplateAndThrownException() throws Throwable {
        // Given

        final StubItem item = mock(StubItem.class);
        final List<String> handlersCalled = new ArrayList<>();
        final PersistenceExceptionHandler<OptimisticLockException> stubOptimisticLockExceptionHandler = new PersistenceExceptionHandler<OptimisticLockException>() {

            @Override
            public void handle(final OptimisticLockException exception) {
                handlersCalled.add(toString());
            }

            @Override
            public String toString() {
                return "OptimisticLockExceptionHandler";
            }

        };
        final PersistenceExceptionHandler<ItemConstraintViolationException> stubItemConstraintViolationExceptionHandler = new PersistenceExceptionHandler<ItemConstraintViolationException>() {

            @Override
            public void handle(final ItemConstraintViolationException exception) {
                handlersCalled.add(toString());
            }

            @Override
            public String toString() {
                return "ItemConstraintViolationExceptionHandler";
            }

        };
        final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlers = new ArrayList<>();
        persistenceExceptionHandlers.add(stubOptimisticLockExceptionHandler);
        persistenceExceptionHandlers.add(stubItemConstraintViolationExceptionHandler);
        final DatabaseTemplate mockDatabaseTemplate = mock(DatabaseTemplate.class);
        final OptimisticLockException optimisticLockException = new OptimisticLockException(null);
        when(mockDatabaseTemplate.create(item)).thenThrow(optimisticLockException);
        final CreateAction<StubItem> createAction = new CreateAction<StubItem>(item, persistenceExceptionHandlers);

        // When
        createAction.apply(mockDatabaseTemplate);

        // Then
        assertThat(handlersCalled.size(), is(1));
        assertThat(handlersCalled.iterator().next(), is("OptimisticLockExceptionHandler"));
    }

    @Test
    public void shouldApply_withDatabaseTemplateAndThrownParentException() throws Throwable {
        // Given

        final StubItem item = mock(StubItem.class);
        final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlers = new ArrayList<>();
        final List<String> handlersCalled = new ArrayList<>();
        final PersistenceExceptionHandler<PersistenceException> stubPersistenceExceptionHandler = new PersistenceExceptionHandler<PersistenceException>() {

            @Override
            public void handle(final PersistenceException exception) {
                handlersCalled.add(toString());
            }

            @Override
            public String toString() {
                return "PersistenceExceptionHandler";
            }

        };
        final PersistenceExceptionHandler<ItemConstraintViolationException> stubItemConstraintViolationExceptionHandler = new PersistenceExceptionHandler<ItemConstraintViolationException>() {

            @Override
            public void handle(final ItemConstraintViolationException exception) {
                handlersCalled.add(toString());
            }

            @Override
            public String toString() {
                return "ItemConstraintViolationExceptionHandler";
            }

        };
        persistenceExceptionHandlers.add(stubPersistenceExceptionHandler);
        persistenceExceptionHandlers.add(stubItemConstraintViolationExceptionHandler);
        final DatabaseTemplate mockDatabaseTemplate = mock(DatabaseTemplate.class);
        final OptimisticLockException optimisticLockException = new OptimisticLockException(null);
        when(mockDatabaseTemplate.create(item)).thenThrow(optimisticLockException);
        final CreateAction<StubItem> createAction = new CreateAction<StubItem>(item, persistenceExceptionHandlers);

        // When
        createAction.apply(mockDatabaseTemplate);

        // Then
        assertThat(handlersCalled.size(), is(1));
        assertThat(handlersCalled.iterator().next(), is("PersistenceExceptionHandler"));
    }
}
