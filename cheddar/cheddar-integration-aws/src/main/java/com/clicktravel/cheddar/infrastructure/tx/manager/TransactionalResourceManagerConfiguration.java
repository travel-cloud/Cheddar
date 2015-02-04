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
package com.clicktravel.cheddar.infrastructure.tx.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.clicktravel.cheddar.infrastructure.messaging.tx.TransactionalMessagePublisher;
import com.clicktravel.cheddar.infrastructure.messaging.tx.TransactionalMessageSender;
import com.clicktravel.cheddar.infrastructure.persistence.database.tx.TransactionalDatabaseTemplate;
import com.clicktravel.cheddar.infrastructure.persistence.filestore.tx.TransactionalFileStore;
import com.clicktravel.cheddar.infrastructure.tx.TransactionalResourceManager;

@Configuration
public class TransactionalResourceManagerConfiguration {

    @Autowired(required = false)
    private TransactionalDatabaseTemplate transactionalDatabaseTemplate;
    @Autowired(required = false)
    private TransactionalFileStore transactionalFileStore;
    @Autowired(required = false)
    private TransactionalMessageSender transactionalMessageSender;
    @Autowired(required = false)
    private TransactionalMessagePublisher transactionalMessagePublisher;

    @Bean
    @Autowired
    public TransactionalResourceManager transactionalResourceManager() {
        final TransactionalResourceManager transactionalResourceManager = new SimpleTransactionalResourceManager(
                transactionalDatabaseTemplate, transactionalFileStore, transactionalMessageSender,
                transactionalMessagePublisher);
        return transactionalResourceManager;
    }

}
