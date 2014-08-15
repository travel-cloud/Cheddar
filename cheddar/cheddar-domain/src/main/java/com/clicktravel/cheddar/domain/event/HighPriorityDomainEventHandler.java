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
 * Handler for a {@link DomainEvent} that should not be deferred and requires prompt processing. High priority event
 * handlers are used for processing that should occur within a short time, such as for completing the response for a
 * dependent REST request.
 */
public interface HighPriorityDomainEventHandler extends DomainEventHandler {

}
