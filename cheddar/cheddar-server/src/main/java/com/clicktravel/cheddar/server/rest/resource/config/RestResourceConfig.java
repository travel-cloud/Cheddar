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
package com.clicktravel.cheddar.server.rest.resource.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import com.clicktravel.common.http.application.ObjectMapperProvider;

@Component
public class RestResourceConfig extends ResourceConfig {

    final ClassPathScanningCandidateComponentProvider scanner;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public RestResourceConfig(final ApplicationContext applicationContext) {
        property("contextConfig", applicationContext);
        scanner = new ClassPathScanningCandidateComponentProvider(true);
        scanner.resetFilters(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Path.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Provider.class));
        register(RequestContextFilter.class);
        register(MultiPartFeature.class);
        register(ObjectMapperProvider.class);
        register(JacksonFeature.class);
        register(new LoggingFeature(
                java.util.logging.Logger.getLogger("com.clicktravel.cheddar.server.http.filter.logging")));
        registerResources("com.clicktravel.cheddar.rest.exception.mapper", "com.clicktravel.cheddar.server.http.filter",
                "com.clicktravel.cheddar.server.rest.resource.status", "com.clicktravel.services");
        property(ServerProperties.LOCATION_HEADER_RELATIVE_URI_RESOLUTION_DISABLED, true);
    }

    private void registerResources(final String... packages) {
        final Collection<String> resourceClassNames = getResourceClassNames(packages);
        resourceClassNames.addAll(Arrays.asList("io.swagger.jaxrs.listing.SwaggerSerializers",
                "com.clicktravel.cheddar.server.rest.SwaggerSpecResource"));

        for (final String resourceClassName : resourceClassNames) {
            try {
                register(Class.forName(resourceClassName));
            } catch (final ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        logger.debug("Registered the following resources: " + resourceClassNames);
    }

    private Collection<String> getResourceClassNames(final String[] packageNames) {

        final Collection<String> resourceClassNames = new ArrayList<>();
        for (final String packageName : packageNames) {
            for (final BeanDefinition bd : scanner.findCandidateComponents(packageName)) {
                resourceClassNames.add(bd.getBeanClassName());
            }
        }
        return resourceClassNames;
    }
}
