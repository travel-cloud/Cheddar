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

import java.util.Optional;

/**
 * Interface for retrieving information describing a security context. A security context may identify a user (also
 * known as the principal), a team which the user is acting within and an agent user which is issuing requests on behalf
 * of the user.
 *
 * @see {@link DefaultSecurityContext} and {@link NullSecurityContext}
 */
public interface SecurityContext {

    /**
     * @return User ID of the principal.
     * @deprecated Use {@link #userId} instead
     */
    @Deprecated
    String principal();

    /**
     * @return Optional of the user ID in the security context. Domain actions (commands or queries) are performed 'as'
     *         this user. This is the actor which any authorisation checks should be performed against.
     */
    Optional<String> userId();

    /**
     * @return Optional of the team ID in the security context. Domain actions (commands or queries) are performed 'as'
     *         a user in this team.
     */
    Optional<String> teamId();

    /**
     * @return Optional of the agent's user ID in the security context. Agents issue requests on behalf of the user
     *         identified by {@link #userId}. Note: This is not the user which authorisation checks are performed
     *         against, see {@link #userId}
     */
    Optional<String> agentUserId();
}
