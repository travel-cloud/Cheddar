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

import java.util.List;

import com.clicktravel.cheddar.infrastructure.persistence.database.DatabaseTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceException;

public class CreateAction<T extends Item> extends DatabaseAction<T> {

    public CreateAction(final T item, final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlers) {
        super(item, persistenceExceptionHandlers);
    }

    @Override
    public void apply(final DatabaseTemplate databaseTemplate) throws Throwable {
        final T item = item();
        item.setVersion(null);
        try {
            databaseTemplate.create(item());
        } catch (final PersistenceException e) {
            handlePersistenceException(e);
        }
    }

}
