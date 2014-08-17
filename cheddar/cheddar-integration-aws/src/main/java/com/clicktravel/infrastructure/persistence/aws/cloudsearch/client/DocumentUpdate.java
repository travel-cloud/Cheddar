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
package com.clicktravel.infrastructure.persistence.aws.cloudsearch.client;

import java.util.ArrayList;
import java.util.Collection;

public class DocumentUpdate {

    private final String id;
    private final Type type;
    private final Collection<Field> fields = new ArrayList<>();

    public enum Type {
        ADD,
        DELETE
    }

    public DocumentUpdate(final Type type, final String id) {
        this.type = type;
        this.id = id;
    }

    public DocumentUpdate withFields(final Collection<Field> fields) {
        this.fields.addAll(fields);
        return this;
    }

    public Type getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public Collection<Field> getFields() {
        return fields;
    }

}
