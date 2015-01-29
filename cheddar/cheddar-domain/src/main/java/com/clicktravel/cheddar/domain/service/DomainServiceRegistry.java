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
package com.clicktravel.cheddar.domain.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainServiceRegistry {

    private static Logger logger = LoggerFactory.getLogger(DomainServiceRegistry.class);
    private static final Map<Class<? extends DomainService>, DomainService> DOMAIN_SERVICES = new HashMap<>();

    public static void init(final Collection<? extends DomainService> domainServices) {
        final StringBuilder sb = new StringBuilder();
        DOMAIN_SERVICES.clear();
        for (final DomainService domainService : domainServices) {
            DOMAIN_SERVICES.put(domainService.getClass(), domainService);
            if (sb.length() != 0) {
                sb.append(", ");
            }
            sb.append(domainService.getClass().getSimpleName());
        }
        logger.debug("Registered the following domain services: [" + sb.toString() + "]");
    }

    @SuppressWarnings("unchecked")
    public static <T extends DomainService> T getService(final Class<T> domainServiceClass) {
        if (domainServiceClass.equals(DomainService.class)) {
            throw new IllegalArgumentException("No matching DomainService found for: " + domainServiceClass);
        }
        final DomainService matchingDomainServiceImplementation = DOMAIN_SERVICES.get(domainServiceClass);
        if (matchingDomainServiceImplementation != null) {
            return (T) matchingDomainServiceImplementation;
        } else {
            for (final Entry<Class<? extends DomainService>, DomainService> entry : DOMAIN_SERVICES.entrySet()) {
                if (domainServiceClass.isAssignableFrom(entry.getKey())) {
                    DOMAIN_SERVICES.put(domainServiceClass, entry.getValue());
                    return (T) entry.getValue();
                }
            }
        }
        throw new IllegalArgumentException("No matching DomainService found for: " + domainServiceClass);
    }

}
