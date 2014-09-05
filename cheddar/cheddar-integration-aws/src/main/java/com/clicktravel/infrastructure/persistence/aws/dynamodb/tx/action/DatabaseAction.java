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
package com.clicktravel.infrastructure.persistence.aws.dynamodb.tx.action;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.clicktravel.cheddar.infrastructure.persistence.database.DatabaseTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.PersistenceException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;

public abstract class DatabaseAction<T> {

    private final T item;
    private final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlers;

    public DatabaseAction(final T item, final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlers) {
        this.item = item;
        this.persistenceExceptionHandlers = new ArrayList<>(persistenceExceptionHandlers);
    }

    public abstract void apply(DatabaseTemplate databaseTemplate) throws Throwable;

    protected void handlePersistenceException(final PersistenceException persistenceException) throws Throwable {
        if (persistenceExceptionHandlers.isEmpty()) {
            throw persistenceException;
        }
        for (final PersistenceExceptionHandler<?> persistenceExceptionHandler : persistenceExceptionHandlers) {
            Method method;
            try {
                method = getPersistenceHandlerMethod(persistenceExceptionHandler, persistenceException);
            } catch (final NoSuchMethodException e) {
                continue;
            }
            try {
                method.setAccessible(true);
                method.invoke(persistenceExceptionHandler, persistenceException);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                continue;
            } catch (final InvocationTargetException handlerException) {
                throw handlerException.getCause();
            }
        }
    }

    private Method getPersistenceHandlerMethod(
            final PersistenceExceptionHandler<? extends PersistenceException> persistenceExceptionHandler,
            final PersistenceException persistenceException) throws NoSuchMethodException {
        for (final Method method : persistenceExceptionHandler.getClass().getMethods()) {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            if (method.getName().equals("handle") && parameterTypes.length == 1
                    && parameterTypes[0].isAssignableFrom(persistenceException.getClass())) {
                return method;
            }
        }
        throw new NoSuchMethodException("No matching 'handle(" + persistenceException.getClass() + ")' method");
    }

    public T item() {
        return item;
    }

    @Override
    public String toString() {
        return "DatabaseAction (" + getClass().getSimpleName() + ") [item=" + item + "]";
    }

}
