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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.*;

import com.clicktravel.cheddar.infrastructure.persistence.document.search.Document;

public class DocumentConfiguration {

    private static final Collection<String> EXCLUDED_PROPERTIES = Arrays.asList("id", "class");
    private final Class<? extends Document> documentClass;
    private final String namespace;
    private final Collection<IndexDefinition> indexDefinitions;
    private final Map<String, PropertyDescriptor> properties; // just used for access

    public DocumentConfiguration(final Class<? extends Document> documentClass, final String namespace) {
        if (documentClass == null) {
            throw new IllegalArgumentException("Document class for DocumentConfiguration must not be empty");
        }
        if (namespace == null || namespace.isEmpty()) {
            throw new IllegalArgumentException("Namespace for DocumentConfiguration must not be empty");
        }
        this.documentClass = documentClass;
        this.namespace = namespace;
        indexDefinitions = new ArrayList<IndexDefinition>();
        properties = new HashMap<>();
        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(documentClass);
            final PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (final PropertyDescriptor prop : propertyDescriptors) {
                if (!EXCLUDED_PROPERTIES.contains(prop.getName())) {
                    properties.put(prop.getName(), prop);
                }
            }
        } catch (final IntrospectionException e) {
            throw new IllegalStateException(e);
        }
    }

    public void registerIndexes(final Collection<IndexDefinition> indexDefinitions) {
        for (final IndexDefinition indexDefinition : indexDefinitions) {
            final String indexPropertyName = indexDefinition.getName();
            if (!properties.containsKey(indexPropertyName)) {
                throw new IllegalStateException("Unable to register index for non-property: " + indexPropertyName);
            }
            this.indexDefinitions.add(indexDefinition);
        }
    }

    public Class<? extends Document> documentClass() {
        return documentClass;
    }

    public String namespace() {
        return namespace;
    }

    public Map<String, PropertyDescriptor> properties() {
        return Collections.unmodifiableMap(properties);
    }

    public Collection<IndexDefinition> indexDefinitions() {
        return Collections.unmodifiableCollection(indexDefinitions);
    }

}
