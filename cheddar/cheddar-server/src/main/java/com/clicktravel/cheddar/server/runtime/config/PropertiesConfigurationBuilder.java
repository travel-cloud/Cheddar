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
package com.clicktravel.cheddar.server.runtime.config;

import java.util.LinkedList;
import java.util.List;

import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.clicktravel.cheddar.domain.feature.toggle.FeatureRegistry;

public abstract class PropertiesConfigurationBuilder {

    public static PropertySourcesPlaceholderConfigurer configurer(final boolean isDevProfileActive,
            final String servicePropertiesPath) {
        final String environmentPropertiesPath = "com.clicktravel.services.env.properties";
        FeatureRegistry.init(environmentPropertiesPath);
        final PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
        configurer.setIgnoreResourceNotFound(true);

        // Note : Later property resources in this list override earlier ones
        final List<Resource> resources = new LinkedList<>();
        if (isDevProfileActive) {
            addResource(resources, "local-" + servicePropertiesPath); // TODO Remove when migrated to dev
            addResource(resources, "dev-" + servicePropertiesPath);
        } else {
            addResource(resources, servicePropertiesPath);
        }
        addResource(resources, "com.clicktravel.cheddar.server.properties");
        addResource(resources, environmentPropertiesPath);

        configurer.setLocations(resources.toArray(new Resource[0]));
        return configurer;
    }

    private static void addResource(final List<Resource> resources, final String path) {
        resources.add(new ClassPathResource(path));
    }
}
