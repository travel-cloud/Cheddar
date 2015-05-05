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

/**
 * Identifies an effective principal as the principal in the security context, and a supporting agent principal. This is
 * used when an agent acts on behalf of (i.e. impersonates) a supported user.
 *
 * @see BasicSecurityContext
 */
public class AgentSecurityContext implements SecurityContext {

    private final String effectivePrincipal;
    private final String agent;

    public AgentSecurityContext(final String effectivePrincipal, final String agent) {
        this.effectivePrincipal = effectivePrincipal;
        this.agent = agent;
    }

    @Override
    public String principal() {
        return effectivePrincipal;
    }

    /**
     * Returns the agent's user id who is acting on behalf of the principal
     * @return
     */
    public String agent() {
        return agent;
    }

}
