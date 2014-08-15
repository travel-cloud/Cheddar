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
package com.clicktravel.cheddar.infrastructure.persistence.document.search.configuration;

import java.util.ArrayList;
import java.util.Collection;

public class DocumentConfigurationHolder {

    private final String schemaName;
    private final Collection<DocumentConfiguration> documentConfigurations;

    public DocumentConfigurationHolder(final String schemaName,
            final Collection<DocumentConfiguration> documentConfigurations) {
        if (schemaName == null || schemaName.isEmpty()) {
            throw new IllegalArgumentException("Schema name must not be empty");
        }
        if (documentConfigurations == null || documentConfigurations.isEmpty()) {
            throw new IllegalArgumentException("Document configurations name must not be empty");
        }
        this.schemaName = schemaName;
        this.documentConfigurations = new ArrayList<>(documentConfigurations);
    }

    public String schemaName() {
        return schemaName;
    }

    public Collection<DocumentConfiguration> documentConfigurations() {
        return documentConfigurations;
    }

}
