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
package com.clicktravel.cheddar.application.tx;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.infrastructure.tx.TransactionException;
import com.clicktravel.cheddar.infrastructure.tx.TransactionalResourceManager;

@Aspect
@Component
public class TransactionalAspect {

    private final TransactionalResourceManager transactionalResourceManager;

    @Autowired
    public TransactionalAspect(final TransactionalResourceManager transactionalResourceManager) {
        this.transactionalResourceManager = transactionalResourceManager;
    }

    // Runs before the advised method
    @Before("@annotation(com.clicktravel.cheddar.application.tx.Transactional)")
    public void beginTransaction() throws TransactionException {
        transactionalResourceManager.begin();
    }

    // Runs after beginTransaction() and advised method, if no exceptions were thrown by either method
    @AfterReturning("@annotation(com.clicktravel.cheddar.application.tx.Transactional)")
    public void commitTransaction() throws TransactionException {
        transactionalResourceManager.commit();
    }

    // Runs after any exception is thrown during beginTransaction(), the advised method, or commitTransaction()
    @AfterThrowing("@annotation(com.clicktravel.cheddar.application.tx.Transactional)")
    public void abortTransaction() {
        transactionalResourceManager.abort();
    }
}
