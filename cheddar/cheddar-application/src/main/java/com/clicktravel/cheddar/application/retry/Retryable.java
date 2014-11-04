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
package com.clicktravel.cheddar.application.retry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods which are automatically retried if they throw an exception. Various options can be controlled
 * with parameters on this annotation:
 * <ul>
 * <li>The maximum number of attempts made before giving up. In the case where all attempts fail, the exception thrown
 * on the last attempt is passed back to the method caller.</li>
 * <li>The duration between successive attempts</li>
 * <li>The exception types for which if thrown by the method should not result in any further retries</li>
 * <li>Named methods (in the same class) which act as exception handlers for the retryable method</li>
 * </ul>
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface Retryable {

    /**
     * @return Maximum number of attempts to call retryable method without exception thrown before giving up
     */
    int maxAttempts() default 5;

    /**
     * @return Delay between method retries, in milliseconds
     */
    int retryDelayMillis() default 2000;

    /**
     * @return Exception classes which should result in immediate failure of the retryable method, without any further
     *         retries.
     */
    Class<? extends Throwable>[] failImmediatelyOn() default {};

    /**
     * @return Names of candidate methods which handle the final exception thrown, in the case the method has failed all
     *         attempts. The method signature of exception handlers must take the exception to handle as the first
     *         parameter, and the remaining parameters are the same as those passed to the retryable method.
     */
    String[] exceptionHandlers() default {};
}
