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
package com.clicktravel.cheddar.infrastructure.persistence.database.query;

import com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceException;

public class NonExistentAttributeException extends PersistenceException {

    private static final long serialVersionUID = 1L;

    public NonExistentAttributeException(final String attributeName, final Class<?> clazz) {
        super(String.format("Attribute %s does not exist in class %s", attributeName, clazz.getCanonicalName()));
    }

}
