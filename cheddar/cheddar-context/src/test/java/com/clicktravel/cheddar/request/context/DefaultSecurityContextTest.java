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
package com.clicktravel.cheddar.request.context;

import static com.clicktravel.common.random.Randoms.randomBoolean;
import static com.clicktravel.common.random.Randoms.randomId;
import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

public class DefaultSecurityContextTest {

    @Test
    public void shouldReturnUserId_withUserInContext() {
        // Given
        final String userId = randomId();
        final String teamId = randomBoolean() ? null : randomId();
        final String identityProviderId = randomBoolean() ? null : randomId();
        final String agentUserId = randomBoolean() ? null : randomId();
        final String appId = randomBoolean() ? null : randomId();
        final DefaultSecurityContext context = new DefaultSecurityContext(userId, teamId, identityProviderId,
                agentUserId, appId);

        // When
        final Optional<String> returnedUserId = context.userId();

        // Then
        assertEquals(Optional.of(userId), returnedUserId);
    }

    @Test
    public void shouldReturnTeamId_withTeamInContext() {
        // Given
        final String userId = randomId();
        final String teamId = randomId();
        final String identityProviderId = randomBoolean() ? null : randomId();
        final String agentUserId = randomBoolean() ? null : randomId();
        final String appId = randomBoolean() ? null : randomId();
        final DefaultSecurityContext context = new DefaultSecurityContext(userId, teamId, identityProviderId,
                agentUserId, appId);

        // When
        final Optional<String> returnedTeamId = context.teamId();

        // Then
        assertEquals(Optional.of(teamId), returnedTeamId);
    }

    @Test
    public void shouldReturnIdentityProviderId_withIdentityProviderInContext() {
        // Given
        final String userId = randomId();
        final String teamId = randomId();
        final String identityProviderId = randomId();
        final String agentUserId = randomBoolean() ? null : randomId();
        final String appId = randomBoolean() ? null : randomId();
        final DefaultSecurityContext context = new DefaultSecurityContext(userId, teamId,
                agentUserId, appId, identityProviderId);

        // When
        final Optional<String> returnedIdentityProviderId = context.identityProviderId();

        // Then
        assertEquals(Optional.of(identityProviderId), returnedIdentityProviderId);
    }

    @Test
    public void shouldReturnAgentUserId_withAgentInContext() {
        // Given
        final String userId = randomId();
        final String teamId = randomBoolean() ? null : randomId();
        final String identityProviderId = randomBoolean() ? null : randomId();
        final String agentUserId = randomId();
        final String appId = randomBoolean() ? null : randomId();
        final DefaultSecurityContext context = new DefaultSecurityContext(userId, teamId,
                agentUserId, appId, identityProviderId);

        // When
        final Optional<String> returnedAgentUserId = context.agentUserId();

        // Then
        assertEquals(Optional.of(agentUserId), returnedAgentUserId);
    }

    @Test
    public void shouldReturnEmptyUserId_withNoUserInContext() {
        // Given
        final String userId = null;
        final String teamId = null;
        final String identityProviderId = null;
        final String agentUserId = null;
        final String appId = null;
        final DefaultSecurityContext context = new DefaultSecurityContext(userId, teamId,
                agentUserId, appId, identityProviderId);

        // When
        final Optional<String> returnedUserId = context.userId();

        // Then
        assertEquals(Optional.empty(), returnedUserId);
    }

    @Test
    public void shouldReturnAppId() {
        // Given
        final String userId = null;
        final String teamId = null;
        final String identityProviderId = null;
        final String agentUserId = null;
        final String appId = randomId();
        final DefaultSecurityContext context = new DefaultSecurityContext(userId, teamId,
                agentUserId, appId, identityProviderId);

        // When
        final Optional<String> returnedUserId = context.appId();

        // Then
        assertEquals(appId, returnedUserId.get());
    }

    @Test
    public void shouldReturnEmptyAgentUserId_withNoAgentInContext() {
        // Given
        final String userId = randomId();
        final String teamId = randomBoolean() ? null : randomId();

        // When
        final DefaultSecurityContext context = new DefaultSecurityContext(userId, teamId);

        // Then
        assertEquals(Optional.empty(), context.agentUserId());
    }

    @Test
    public void shouldReturnEmptyTeamIdAndEmptyAgentId_withNoTeamNorAgentInContext() {
        // Given
        final String userId = randomId();

        // When
        final DefaultSecurityContext context = new DefaultSecurityContext(userId);

        // Then
        assertEquals(Optional.empty(), context.teamId());
        assertEquals(Optional.empty(), context.agentUserId());
    }

    @Test
    public void shouldReturnHeaders_withNoIdentityProviderIdInContext() {
        // Given
        final String userId = randomId();
        final String teamId = randomId();
        final String agentUserId = randomId();
        final String appId = randomId();
        final DefaultSecurityContext context = new DefaultSecurityContext(userId, teamId,
                agentUserId, appId);

        // When
        final Optional<String> returnedUserId = context.userId();
        final Optional<String> returnedTeamId = context.teamId();
        final Optional<String> returnedAgentUserId = context.agentUserId();
        final Optional<String> returnedAppId = context.appId();

        // Then
        assertEquals(Optional.of(userId), returnedUserId);
        assertEquals(Optional.of(teamId), returnedTeamId);
        assertEquals(Optional.of(agentUserId), returnedAgentUserId);
        assertEquals(Optional.of(appId), returnedAppId);
    }

    @Test
    public void shouldReturnHeaders_withIdentityProviderIdInContext() {
        // Given
        final String userId = randomId();
        final String teamId = randomId();
        final String agentUserId = randomId();
        final String appId = randomId();
        final String identityProviderId = randomId();
        final DefaultSecurityContext context = new DefaultSecurityContext(userId, teamId,
                agentUserId, appId, identityProviderId);

        // When
        final Optional<String> returnedUserId = context.userId();
        final Optional<String> returnedTeamId = context.teamId();
        final Optional<String> returnedAgentUserId = context.agentUserId();
        final Optional<String> returnedAppId = context.appId();
        final Optional<String> returnedIdentityProviderId = context.identityProviderId();

        // Then
        assertEquals(Optional.of(userId), returnedUserId);
        assertEquals(Optional.of(teamId), returnedTeamId);
        assertEquals(Optional.of(agentUserId), returnedAgentUserId);
        assertEquals(Optional.of(appId), returnedAppId);
        assertEquals(Optional.of(identityProviderId), returnedIdentityProviderId);
    }
}
