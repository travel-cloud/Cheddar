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
package com.clicktravel.cheddar.server.application.status;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.server.application.configuration.ApplicationConfiguration;
import com.clicktravel.cheddar.system.event.ApplicationInstanceStartedEvent;
import com.clicktravel.cheddar.system.event.publisher.SystemEventPublisher;

/**
 * Publishes an {@link ApplicationInstanceStartedEvent} when this application instance starts
 */
@Component
public class ApplicationInstanceStartedEventPublisher implements ApplicationListener<ContextRefreshedEvent> {

    private final String applicationInstanceName;

    @Autowired
    public ApplicationInstanceStartedEventPublisher(final ApplicationConfiguration applicationConfiguration) {
        applicationInstanceName = applicationConfiguration.name();
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        final ApplicationInstanceStartedEvent applicationInstanceStartedEvent = new ApplicationInstanceStartedEvent();
        applicationInstanceStartedEvent.setApplicationInstanceName(applicationInstanceName);
        SystemEventPublisher.instance().publishEvent(applicationInstanceStartedEvent);
    }
}
