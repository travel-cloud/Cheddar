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
     * Checks that the given principal matches the principal currently set in the security context. If they do not match
     * an exception is thrown.
     * @param principal
     */
    public static void checkPrincipal(final String principal) {
        final String currentPrincipal = SecurityContextHolder.getPrincipal();
        if (currentPrincipal == null) {
            throw new CredentialsMissingException();
        }
        if (!currentPrincipal.equals(principal)) {
            throw new SecurityConstraintViolationException("Access denied for principal: " + currentPrincipal);
        }
    }

    /**
     * Checks that there is a principal in the current context i.e. a user is authenticated
     */
    public static void checkAuthenticated() {
        if (SecurityContextHolder.getPrincipal() == null) {
            throw new CredentialsMissingException();
        }
    }

}
