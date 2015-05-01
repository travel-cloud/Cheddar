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
 * Retains the security context for the scope of a request.
 */
public class SecurityContextHolder {

    private final static ThreadLocal<SecurityContext> SECURITY_CONTEXT = new ThreadLocal<SecurityContext>() {
    };

    public static void set(final SecurityContext securityContext) {
        SECURITY_CONTEXT.set(securityContext);
    }

    public static SecurityContext get() {
        return SECURITY_CONTEXT.get();
    }

    public static void clear() {
        SECURITY_CONTEXT.remove();
    }

    /**
     * @deprecated Use SecurityContextHolder.get().principal();
     */
    @Deprecated
    public String getPrincipal() {
        final SecurityContext securityContext = get();
        if (securityContext == null) {
            return null;
        }
        return securityContext.principal();
    }

    /**
     * @deprecated Use SecurityContextHolder.set(SecurityContext)
     */
    @Deprecated
    public void setPrincipal(final String principal) {
        if (principal == null) {
            clear();
        } else {
            set(new BasicSecurityContext(principal));
        }
    }
}
