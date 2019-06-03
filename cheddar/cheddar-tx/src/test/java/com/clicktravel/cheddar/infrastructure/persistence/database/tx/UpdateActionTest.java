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

import static com.clicktravel.common.random.Randoms.randomLong;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.clicktravel.cheddar.infrastructure.persistence.database.DatabaseTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.ItemConstraintViolationException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.OptimisticLockException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceException;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceResourceFailureException;

@SuppressWarnings({ "unchecked" })
public class UpdateActionTest {

    @Test
    public void shouldConstructUpdateAction_withItemAndPersistenceExceptionHandlers() {
        // Given
        final StubItem item = new StubItem();
        final PersistenceExceptionHandler<ItemConstraintViolationException> mockItemConstraintViolationExceptionHandler = mock(
                PersistenceExceptionHandler.class);
        final PersistenceExceptionHandler<PersistenceResourceFailureException> mockPersistenceResourceFailureExceptionHandler = mock(
                PersistenceExceptionHandler.class);
        final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlers = Arrays
                .asList(mockItemConstraintViolationExceptionHandler, mockPersistenceResourceFailureExceptionHandler);

        // When
        final UpdateAction<StubItem> updateAction = new UpdateAction<>(item, persistenceExceptionHandlers);

        // Then
        assertNotNull(updateAction);
        assertThat(updateAction.item(), is(item));
    }

    @Test
    public void shouldApplyUpdate_withDatabaseTemplate() throws Throwable {
        // Given
        final StubItem item = new StubItem();
        final Long itemVersion = randomLong();
        final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlers = Collections.EMPTY_LIST;
        final UpdateAction<StubItem> updateAction = new UpdateAction<>(item, persistenceExceptionHandlers);
        final DatabaseTemplate mockDatabaseTemplate = mock(DatabaseTemplate.class);

        item.setVersion(itemVersion);

        // When
        updateAction.apply(mockDatabaseTemplate);

        // Then
        verify(mockDatabaseTemplate).update(item);

        assertThat(item.getVersion(), is(itemVersion - 1));
    }

    @Test
    public void shouldNotApplyUpdate_withPersistenceExceptionThrownAndNoHandlers() throws Throwable {
        // Given
        final StubItem item = new StubItem();
        final Long itemVersion = randomLong();
        final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlers = Collections.EMPTY_LIST;
        final UpdateAction<StubItem> updateAction = new UpdateAction<>(item, persistenceExceptionHandlers);
        final DatabaseTemplate mockDatabaseTemplate = mock(DatabaseTemplate.class);
        final OptimisticLockException thrownException = new OptimisticLockException("test");

        item.setVersion(itemVersion);
        when(mockDatabaseTemplate.update(item)).thenThrow(thrownException);

        // When
        OptimisticLockException actualException = null;

        try {
            updateAction.apply(mockDatabaseTemplate);
        } catch (final OptimisticLockException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
        assertThat(actualException, is(thrownException));
    }

    @Test
    public void shouldNotApplyUpdateAndHandleException_withPersistenceExceptionThrownAndMatchingExceptionHandler()
            throws Throwable {
        // Given
        final StubItem item = new StubItem();
        final Long itemVersion = randomLong();
        final PersistenceExceptionHandler<ItemConstraintViolationException> mockItemConstraintViolationExceptionHandler = (PersistenceExceptionHandler<ItemConstraintViolationException>) createMockPersistenceExceptionHandler(
                ItemConstraintViolationException.class, UpdateActionRuntimeException.class);
        final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlers = Arrays
                .asList(mockItemConstraintViolationExceptionHandler);
        final UpdateAction<StubItem> updateAction = new UpdateAction<>(item, persistenceExceptionHandlers);
        final DatabaseTemplate mockDatabaseTemplate = mock(DatabaseTemplate.class);

        item.setVersion(itemVersion);
        when(mockDatabaseTemplate.update(item)).thenThrow(ItemConstraintViolationException.class);

        // When
        UpdateActionRuntimeException actualExcepion = null;
        try {
            updateAction.apply(mockDatabaseTemplate);
        } catch (final UpdateActionRuntimeException e) {
            actualExcepion = e;
        }

        // Then
        verify(mockItemConstraintViolationExceptionHandler).getExceptionClass();
        verify(mockItemConstraintViolationExceptionHandler).handle(any(ItemConstraintViolationException.class));

        assertNotNull(actualExcepion);
    }

    @Test
    public void shouldNotApplyUpdate_withPersistenceExceptionThrownAndNoMatchingExceptionHandlers() throws Throwable {
        // Given
        final StubItem item = new StubItem();
        final Long itemVersion = randomLong();
        final PersistenceExceptionHandler<ItemConstraintViolationException> mockItemConstraintViolationExceptionHandler = (PersistenceExceptionHandler<ItemConstraintViolationException>) createMockPersistenceExceptionHandler(
                ItemConstraintViolationException.class);
        final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlers = Arrays
                .asList(mockItemConstraintViolationExceptionHandler);
        final UpdateAction<StubItem> updateAction = new UpdateAction<>(item, persistenceExceptionHandlers);
        final DatabaseTemplate mockDatabaseTemplate = mock(DatabaseTemplate.class);
        final OptimisticLockException thrownException = new OptimisticLockException("test");

        item.setVersion(itemVersion);
        when(mockDatabaseTemplate.update(item)).thenThrow(thrownException);

        // When
        OptimisticLockException actualException = null;
        try {
            updateAction.apply(mockDatabaseTemplate);
        } catch (final OptimisticLockException e) {
            actualException = e;
        }

        // Then
        verify(mockItemConstraintViolationExceptionHandler).getExceptionClass();
        verify(mockItemConstraintViolationExceptionHandler, never()).handle(any());

        assertNotNull(actualException);
        assertThat(actualException, is(thrownException));
    }

    private PersistenceExceptionHandler<? extends PersistenceException> createMockPersistenceExceptionHandler(
            final Class<? extends PersistenceException> persistenceExceptionClass) {
        return createMockPersistenceExceptionHandler(persistenceExceptionClass, null);
    }

    private PersistenceExceptionHandler<? extends PersistenceException> createMockPersistenceExceptionHandler(
            final Class<? extends PersistenceException> persistenceExceptionClass,
            final Class<? extends PersistenceException> exceptionClassToThrow) {
        final PersistenceExceptionHandler<? extends PersistenceException> mockPersistenceExceptionHandler = mock(
                PersistenceExceptionHandler.class);
        if (exceptionClassToThrow != null) {
            doThrow(exceptionClassToThrow).when(mockPersistenceExceptionHandler).handle(any());
        }
        when(mockPersistenceExceptionHandler.getExceptionClass())
                .thenAnswer(invocationOnMock -> persistenceExceptionClass);
        return mockPersistenceExceptionHandler;
    }

    private class UpdateActionRuntimeException extends PersistenceException {
        public UpdateActionRuntimeException(final String message) {
            super(message);
        }

        private static final long serialVersionUID = 1L;
    }

}
