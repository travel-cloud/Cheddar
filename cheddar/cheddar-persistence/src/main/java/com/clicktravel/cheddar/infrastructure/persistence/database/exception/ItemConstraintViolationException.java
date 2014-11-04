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

import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceException;

/**
 * Exception to be used in the persistence layer when an item-level constraint has been violated, such as a violation of
 * a unique valued attribute constraint.
 */
public class ItemConstraintViolationException extends PersistenceException {

    private static final long serialVersionUID = 3975221821260678353L;
    private final String propertyName;

    public ItemConstraintViolationException(final String propertyName, final String message) {
        super(message);
        this.propertyName = propertyName;
    }

    public String propertyName() {
        return propertyName;
    }

}
