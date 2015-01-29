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

import com.clicktravel.common.functional.Equals;

/**
 * Retains security credentials for the scope of a request. The principal is the actor that initiated the request. In
 * the case of users, the principal is identified by the user account Id.
 */
public class SecurityContextHolder {

    private final static ThreadLocal<String> context = new ThreadLocal<String>() {
    };

    public static void setPrincipal(final String principal) {
        if (Equals.isNullOrBlank(principal)) {
            clearPrincipal();
        } else {
            context.set(principal);
        }
    }

    public static void clearPrincipal() {
        context.remove();
    }

    public static String getPrincipal() {
        return context.get();
    }

}
