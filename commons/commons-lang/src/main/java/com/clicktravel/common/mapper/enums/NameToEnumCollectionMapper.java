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
package com.clicktravel.common.mapper.enums;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.clicktravel.common.mapper.CollectionMapper;
import com.clicktravel.common.mapper.Mapper;
import com.clicktravel.common.mapper.MapperException;

public class NameToEnumCollectionMapper<E extends Enum<E>> extends CollectionMapper<String, E> {

    public NameToEnumCollectionMapper(final Class<E> enumClass) {

        super(new Mapper<String, E>() {

            Map<String, E> enumNames;

            @Override
            public E map(final String argument) {
                if (argument == null) {
                    return null;
                }
                final Map<String, E> enumNames = getEnumNames();
                if (enumNames.containsKey(argument)) {
                    return getEnumNames().get(argument);
                }
                throw new MapperException("Unable to map enum for value: " + argument);
            }

            private Map<String, E> getEnumNames() {
                if (enumNames == null) {
                    try {
                        enumNames = new HashMap<>();
                        final Method valueOfStaticMethod = enumClass.getMethod("values");
                        @SuppressWarnings("unchecked")
                        final E[] enumValues = (E[]) valueOfStaticMethod.invoke(enumClass);
                        for (final E e : enumValues) {
                            enumNames.put(e.name(), e);
                        }
                    } catch (final Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
                return enumNames;
            }
        });
    }
}