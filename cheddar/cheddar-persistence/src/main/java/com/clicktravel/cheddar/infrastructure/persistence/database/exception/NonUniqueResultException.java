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
package com.clicktravel.cheddar.infrastructure.persistence.database.exception;

import java.util.Collection;

import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceException;

/**
 * Exception to be used in the persistence layer when the client has asked for a unique value but either none or many
 * have been returned.
 */
public class NonUniqueResultException extends PersistenceException {

    private static final long serialVersionUID = 1444830852428155318L;
    private final String message;
    private final boolean hasResults;

    public NonUniqueResultException(final Class<? extends Item> itemClass, final Collection<? extends Item> items) {
        super(String.format("No unique item found for class [%s].", itemClass));
        if (items == null) {
            throw new IllegalArgumentException("Items must not be null");
        }
        message = super.getMessage() + String.format(" The matching items were : [%s]", items);
        hasResults = !items.isEmpty();
    }

    @Override
    public String getMessage() {
        return message;
    }

    public boolean hasResults() {
        return hasResults;
    }

}
