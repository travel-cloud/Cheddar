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
package com.clicktravel.cheddar.application.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.common.mapper.Mapper;
import com.clicktravel.cheddar.infrastructure.persistence.database.DatabaseTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.database.Item;
import com.clicktravel.cheddar.infrastructure.persistence.database.ItemId;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.NonExistentItemException;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.AttributeQuery;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Condition;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Operators;
import com.clicktravel.cheddar.infrastructure.persistence.database.query.Query;

public class PersistedTracker<T1 extends Process, T2 extends Item> implements Tracker<T1> {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DatabaseTemplate databaseTemplate;
    private final Class<T1> processClass;
    private final Class<T2> processItemClass;
    private final Mapper<T2, T1> itemToProcessMapper;
    private final Mapper<T1, T2> processToItemMapper;

    public PersistedTracker(final DatabaseTemplate databaseTemplate, final Class<T1> processClass,
            final Class<T2> processItemClass, final Mapper<T1, T2> processToItemMapper,
            final Mapper<T2, T1> itemToProcessMapper) {
        this.databaseTemplate = databaseTemplate;
        this.processClass = processClass;
        this.processItemClass = processItemClass;
        this.itemToProcessMapper = itemToProcessMapper;
        this.processToItemMapper = processToItemMapper;
    }

    @Override
    public void startProcess(final T1 process) {
        logger.info("ProcessTracker started with id: " + process.processId().id());
        final T2 processItem = processToItemMapper.map(process);
        databaseTemplate.create(processItem);
    }

    @Override
    public void completeProcess(final T1 process) {
        final T2 processItem = processToItemMapper.map(process);
        databaseTemplate.delete(processItem);
        logger.info("ProcessTracker ended with process: " + processItem);
    }

    @Override
    public T1 processForId(final ProcessId processId) {
        try {
            final T2 processItem = databaseTemplate.read(new ItemId(processId.id()), processItemClass);
            return itemToProcessMapper.map(processItem);
        } catch (final NonExistentItemException e) {
            throw new NonExistentProcessException(processClass, processId);
        }
    }

    @Override
    public T1 processForAttribute(final String attributeName, final String value) {
        final Query query = new AttributeQuery(attributeName, new Condition(Operators.EQUALS, value));
        final T2 processItem = databaseTemplate.fetchUnique(query, processItemClass);
        return itemToProcessMapper.map(processItem);
    }

    @Override
    public void updateProcess(final T1 process) {
        final T2 processItem = processToItemMapper.map(process);
        databaseTemplate.update(processItem);
    }

    @Override
    public void checkProcessDoesNotExist(final ProcessId processId) throws InvalidProcessStatusException {
        try {
            databaseTemplate.read(new ItemId(processId.id()), processItemClass);
            throw new InvalidProcessStatusException(processClass, "Process not started");
        } catch (final NonExistentItemException e) {
            // This is a positive outcome for this method signature
            return;
        }
    }
}
