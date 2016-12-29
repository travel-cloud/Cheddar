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
package com.clicktravel.cheddar.application.security;

import com.clicktravel.cheddar.request.context.SecurityContextHolder;

public class SecurityChecker {

    /**
     * Checks that the given user ID matches the user ID set in the security context. If they do not match an exception
     * is thrown.
     * @param userId
     */
    public static void checkUser(final String userId) {
        checkAnyUser();
        final String contextUserId = SecurityContextHolder.get().userId().get();
        if (!contextUserId.equals(userId)) {
            throw new SecurityConstraintViolationException("Access denied for principal: " + contextUserId);
        }
    }

    /**
     * Checks that the given team ID matches the team ID set in the security context. If they do not match an exception
     * is thrown.
     * @param teamId
     */
    public static void checkTeam(final String teamId) {
        checkAnyTeam();
        final String contextTeamId = SecurityContextHolder.get().teamId().get();
        if (!contextTeamId.equals(teamId)) {
            throw new SecurityConstraintViolationException("Access denied for team: " + contextTeamId);
        }
    }

    /**
     * Checks that there is a user in the security context
     */
    public static void checkAnyUser() {
        if (!SecurityContextHolder.get().userId().isPresent()) {
            throw new CredentialsMissingException();
        }
    }

    /**
     * Checks that there is a team in the security context
     */
    public static void checkAnyTeam() {
        if (!SecurityContextHolder.get().teamId().isPresent()) {
            throw new CredentialsMissingException();
        }
    }
}
