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
package com.clicktravel.cheddar.infrastructure.persistence.database;

import java.util.Collection;

import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonUniqueResultException;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Query;

public abstract class AbstractDatabaseTemplate implements DatabaseTemplate {

    @Override
    public <T extends Item> T fetchUnique(final Query query, final Class<T> itemClass) throws NonUniqueResultException {
        final Collection<T> items = this.fetch(query, itemClass);
        if (items.size() != 1) {
            throw new NonUniqueResultException(itemClass, items);
        }
        return items.iterator().next();
    }

}
