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
package com.clicktravel.cheddar.domain.service.runtime.config;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.clicktravel.cheddar.domain.service.DomainService;
import com.clicktravel.cheddar.domain.service.DomainServiceRegistry;

@Configuration
public class DomainServiceConfiguration {

    @Autowired(required = false)
    private Collection<DomainService> domainServices;

    @PostConstruct
    public void initializeDomainServiceRegistry() {
        if (domainServices != null) {
            DomainServiceRegistry.init(domainServices);
        }
    }

}
