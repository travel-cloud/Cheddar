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
package com.clicktravel.cheddar.domain.event;

import org.springframework.stereotype.Component;

import com.clicktravel.common.remote.AsynchronousExceptionHandler;
import com.clicktravel.cheddar.domain.model.exception.ConstraintViolationException;

/**
 * Handles exceptions thrown from @Asynchronous void methods by checking for and publishing associated domain events.
 * This is intended to publish domain events in the case where a remote call fails all its attempts.
 */
@Component
public class ConstraintViolationDomainEventPublisher implements AsynchronousExceptionHandler {

    public ConstraintViolationDomainEventPublisher() {
    }

    @Override
    public void handle(final Throwable exception) {
        if (exception instanceof ConstraintViolationException) {
            handle((ConstraintViolationException) exception);
        }
    }

    private void handle(final ConstraintViolationException constraintViolationException) {
        final DomainEvent domainEvent = constraintViolationException.getDomainEvent();
        if (domainEvent != null) {
            DomainEventPublisher.instance().publishEvent(domainEvent);
        }
    }

}
