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
package com.clicktravel.cheddar.server.rest.resource.status;

public class MaximumWorkRates {

    private MaximumWorkRate restRequest;
    private MaximumWorkRate highPriorityDomainEventHandler;
    private MaximumWorkRate lowPriorityDomainEventHandler;

    public MaximumWorkRate getRestRequest() {
        return restRequest;
    }

    public void setRestRequest(final MaximumWorkRate restRequest) {
        this.restRequest = restRequest;
    }

    public MaximumWorkRate getHighPriorityDomainEventHandler() {
        return highPriorityDomainEventHandler;
    }

    public void setHighPriorityDomainEventHandler(final MaximumWorkRate highPriorityDomainEventHandler) {
        this.highPriorityDomainEventHandler = highPriorityDomainEventHandler;
    }

    public MaximumWorkRate getLowPriorityDomainEventHandler() {
        return lowPriorityDomainEventHandler;
    }

    public void setLowPriorityDomainEventHandler(final MaximumWorkRate lowPriorityDomainEventHandler) {
        this.lowPriorityDomainEventHandler = lowPriorityDomainEventHandler;
    }

}
