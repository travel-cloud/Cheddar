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

/**
 * Handler for a {@link DomainEvent} that may be deferred. This is the most common case for domain event handling. Low
 * priority event handlers are used for processing which is not urgent and does not have a requirement for prompt
 * processing. Low priority handling is used for tasks like updating views.
 */
public interface LowPriorityDomainEventHandler extends DomainEventHandler {

}
