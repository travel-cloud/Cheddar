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
package com.clicktravel.common.mapper;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A mapper that is applied to an input collection to produce an output collection. This uses an underlying mapper which
 * is applied to members of the input collection to produce members of the output collection.
 * @param <T1> Type of member in input collection
 * @param <T2> Type of member of output collection
 */
public abstract class CollectionMapper<T1, T2> {

    private final Mapper<T1, T2> mapper;

    /**
     * Constructor that sets the mapper used to apply to collection members
     * @param mapper
     */
    public CollectionMapper(final Mapper<T1, T2> mapper) {
        this.mapper = mapper;
    }

    /**
     * Apply the mapper to all members of an input collection to produce an output collection
     * @param inputCollection
     * @return Mapped collection
     * @throws MapperException - if collection cannot be mapped successfully
     */
    public Collection<T2> map(final Collection<T1> inputCollection) throws CollectionElementMapperException {
        final Collection<T2> outputCollection = new ArrayList<>();
        if (inputCollection != null) {
            for (final T1 inputItem : inputCollection) {
                try {
                    outputCollection.add(mapper.map(inputItem));
                } catch (final MapperException mapperException) {
                    handle(mapperException, inputItem);
                }
            }
        }
        return outputCollection;
    }

    /**
     * Handle an input item that could not be mapped. This default implementation simply throws a
     * {@link CollectionElementMapperException}
     * @param cause Exception thrown by underlying mapper
     * @param inputItem Input item that could not be mapped
     */
    protected void handle(final MapperException cause, final T1 inputItem) {
        throw new CollectionElementMapperException(cause, inputItem);
    }

    public Mapper<T1, T2> mapper() {
        return mapper;
    }

}
