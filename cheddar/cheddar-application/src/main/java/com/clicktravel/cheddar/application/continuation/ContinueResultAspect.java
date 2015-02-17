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
package com.clicktravel.cheddar.application.continuation;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(400)
@Component
public class ContinueResultAspect {

    private final ContinuationHandler continuationHandler;

    @Autowired
    public ContinueResultAspect(final ContinuationHandler continuationHandler) {
        this.continuationHandler = continuationHandler;
    }

    @AfterReturning("@annotation(com.clicktravel.cheddar.application.continuation.ContinueResult)")
    public void returnMethodResult() {
        continuationHandler.offerMethodResult();
    }

    @AfterThrowing("@annotation(com.clicktravel.cheddar.application.continuation.ContinueResult)")
    public void abortOfferMethodResult() {
        continuationHandler.abortOfferMethodResult();
    }
}
