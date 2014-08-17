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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;

import org.junit.Test;

public class DomainServiceRegistryTest {

    @Test
    public void shouldGetService_withDomainServiceImplementation() throws Exception {
        // Given
        final StubDomainService stubDomainService = new DefaultTestDomainService();
        DomainServiceRegistry.init(Arrays.asList(stubDomainService));

        // When
        final DefaultTestDomainService returnedDefaultTestDomainService = DomainServiceRegistry
                .getService(DefaultTestDomainService.class);

        // Then
        assertSame(stubDomainService, returnedDefaultTestDomainService);
        assertEquals(DefaultTestDomainService.class, returnedDefaultTestDomainService.getClass());
    }

    @Test
    public void shouldGetService_withDomainServiceInterface() throws Exception {
        // Given
        final StubDomainService stubDomainService = new DefaultTestDomainService();
        DomainServiceRegistry.init(Arrays.asList(stubDomainService));

        // When
        final StubDomainService returnedStubDomainService = DomainServiceRegistry.getService(StubDomainService.class);

        // Then
        assertSame(stubDomainService, returnedStubDomainService);
        assertEquals(DefaultTestDomainService.class, returnedStubDomainService.getClass());
    }

    @Test
    public void shouldNotGetService_withUnknownDomainServiceInterface() throws Exception {
        // Given
        final StubDomainService stubDomainService = new DefaultTestDomainService();
        DomainServiceRegistry.init(Arrays.asList(stubDomainService));

        // When
        IllegalArgumentException actualException = null;
        try {
            DomainServiceRegistry.getService(DomainService.class);
        } catch (final IllegalArgumentException e) {
            actualException = e;
        }

        // Then
        assertNotNull(actualException);
    }

    @Test
    public void shouldInitializeDomainServices_withDomainService() throws Exception {
        // Given
        final DefaultTestDomainService stubDomainService = new DefaultTestDomainService();

        // When
        DomainServiceRegistry.init(Arrays.asList(stubDomainService));

        // Then
        assertSame(stubDomainService, DomainServiceRegistry.getService(DefaultTestDomainService.class));
    }

    @Test
    public void shouldInitializeDomainServices_withDomainServiceReinitialized() throws Exception {
        // Given
        final DefaultTestDomainService newStubDomainService = new DefaultTestDomainService();
        final DefaultTestDomainService oldStubDomainService = new DefaultTestDomainService();
        DomainServiceRegistry.init(Arrays.asList(oldStubDomainService));

        // When
        DomainServiceRegistry.init(Arrays.asList(newStubDomainService));

        // Then
        assertSame(newStubDomainService, DomainServiceRegistry.getService(DefaultTestDomainService.class));
    }

}
