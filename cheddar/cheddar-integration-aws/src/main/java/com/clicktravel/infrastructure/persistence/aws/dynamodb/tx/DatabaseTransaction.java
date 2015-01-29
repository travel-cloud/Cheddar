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
package com.clicktravel.infrastructure.persistence.aws.dynamodb.tx;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import com.clicktravel.cheddar.infrastructure.persistence.database.DatabaseTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.handler.PersistenceExceptionHandler;
import com.clicktravel.cheddar.infrastructure.tx.Transaction;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.tx.action.CreateAction;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.tx.action.DatabaseAction;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.tx.action.DeleteAction;
import com.clicktravel.infrastructure.persistence.aws.dynamodb.tx.action.UpdateAction;

public class DatabaseTransaction implements Transaction {

    private final Queue<DatabaseAction<?>> databaseActions;
    private final String transactionId;

    public DatabaseTransaction() {
        databaseActions = new LinkedList<>();
        transactionId = UUID.randomUUID().toString();
    }

    @Override
    public String transactionId() {
        return transactionId;
    }

    public <T extends Item> T addCreateAction(final T item,
            final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlers) {
        databaseActions.add(new CreateAction<T>(item, persistenceExceptionHandlers));
        item.setVersion(1l);
        return item;
    }

    public <T extends Item> T addUpdateAction(final T item,
            final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlers) {
        databaseActions.add(new UpdateAction<T>(item, persistenceExceptionHandlers));
        item.setVersion(item.getVersion() + 1);
        return item;
    }

    public <T extends Item> void addDeleteAction(final T item,
            final List<PersistenceExceptionHandler<?>> persistenceExceptionHandlers) {
        databaseActions.add(new DeleteAction<T>(item, persistenceExceptionHandlers));
    }

    public void applyActions(final DatabaseTemplate databaseTemplate) throws Throwable {
        while (!databaseActions.isEmpty()) {
            final DatabaseAction<?> databaseAction = databaseActions.remove();
            databaseAction.apply(databaseTemplate);
        }
    }

}
