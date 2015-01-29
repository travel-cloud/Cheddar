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
package com.clicktravel.cheddar.server.http.filter.application.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * A configuration bean for the ApplicationGatewayFilter
 *
 * This single-property bean is required due to the lack of support for using @Inject in combination with @Value to wire
 * in system properties.
 */
@Component
public class ApplicationGatewayFilterConfiguration {

    private final String applicationGatewayToken;

    @Autowired
    public ApplicationGatewayFilterConfiguration(
            @Value("${application.gateway.token}") final String applicationGatewayToken) {
        this.applicationGatewayToken = applicationGatewayToken;
    }

    public String applicationGatewayToken() {
        return applicationGatewayToken;
    }

}
