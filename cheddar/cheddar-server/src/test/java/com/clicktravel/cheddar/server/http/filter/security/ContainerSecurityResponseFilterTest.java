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
package com.clicktravel.cheddar.server.http.filter.security;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.clicktravel.cheddar.request.context.SecurityContextHolder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SecurityContextHolder.class })
public class ContainerSecurityResponseFilterTest {

    @Test
    public void shouldClearPrincipal_onFilter() throws Exception {
        // Given
        mockStatic(SecurityContextHolder.class);
        final ContainerRequestContext mockContainerRequestContext = mock(ContainerRequestContext.class);
        final ContainerResponseContext mockContainerResponseContext = mock(ContainerResponseContext.class);
        final ContainerSecurityResponseFilter containerSecurityResponseFilter = new ContainerSecurityResponseFilter();

        // When
        containerSecurityResponseFilter.filter(mockContainerRequestContext, mockContainerResponseContext);

        // Then
        verifyStatic();
        SecurityContextHolder.clearPrincipal();
    }
}
