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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Order(500)
@Component
public class RetryableAspect {

    /**
     * Exceptions which always result in immediate failure of a remote call attempt without further retries
     */
    private static final List<Class<? extends Throwable>> alwaysImmediateFailureExceptionClasses = new ArrayList<>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @SuppressWarnings("unchecked")
    public RetryableAspect() {
        final String[] exceptionClassNames = { "com.clicktravel.common.validation.ValidationException",
                "com.clicktravel.cheddar.infrastructure.persistence.exception.PersistenceResourceFailureException" };
        try {
            for (final String exceptionClassName : exceptionClassNames) {
                final Class<? extends Throwable> exceptionClass = (Class<? extends Throwable>) Class
                        .forName(exceptionClassName);
                alwaysImmediateFailureExceptionClasses.add(exceptionClass);
            }
        } catch (final Exception e) {
            throw new RuntimeException("Error initialising Retryable aspect", e);
        }
    }

    @Around("@annotation(retryable)")
    public Object attemptMethodAndRetryIfNeeded(final ProceedingJoinPoint proceedingJoinPoint,
            final Retryable retryable) throws Throwable {
        int attempts = 0;
        do {
            try {
                return proceedingJoinPoint.proceed();
            } catch (final Throwable thrownException) {
                attempts++;
                if (shouldRetryMethod(thrownException.getClass(), retryable, attempts)) {
                    Thread.sleep(retryable.retryDelayMillis());
                } else {
                    return processMethodFailure(proceedingJoinPoint, retryable.exceptionHandlers(), thrownException);
                }
            }
        } while (true);
    }

    private boolean shouldRetryMethod(final Class<? extends Throwable> thrownClass, final Retryable retryable,
            final int attempts) {
        if (RetryableUtils.isRetryableDisabled() || attempts >= retryable.maxAttempts()) {
            return false;
        }
        final HashSet<Class<? extends Throwable>> failClasses = new HashSet<>(
                Arrays.asList(retryable.failImmediatelyOn()));
        failClasses.addAll(alwaysImmediateFailureExceptionClasses);
        for (final Class<? extends Throwable> failClass : failClasses) {
            if (failClass.isAssignableFrom(thrownClass)) {
                return false;
            }
        }
        return true;
    }

    private Object processMethodFailure(final ProceedingJoinPoint proceedingJoinPoint,
            final String[] exceptionHandlerNames, final Throwable thrownException) throws Throwable {
        final Method handlerMethod = getHandlerMethod(proceedingJoinPoint, thrownException.getClass(),
                exceptionHandlerNames);
        if (handlerMethod != null) {
            logger.trace("Selected handlerMethod : " + handlerMethod.getName());
            try {
                return handlerMethod.invoke(proceedingJoinPoint.getThis(),
                        getExceptionHandlerArgs(thrownException, proceedingJoinPoint.getArgs()));
            } catch (final InvocationTargetException invocationTargetException) {
                throw invocationTargetException.getCause(); // exception thrown by handler method
            }
        } else {
            throw thrownException;
        }
    }

    private Method getHandlerMethod(final ProceedingJoinPoint proceedingJoinPoint,
            final Class<? extends Throwable> thrownExceptionClass, final String[] handlerMethodNames) {
        final Class<?> targetClass = proceedingJoinPoint.getThis().getClass();
        final MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
        final Class<?>[] handlerParameterTypes = getExceptionHandlerParameterTypes(thrownExceptionClass,
                methodSignature.getParameterTypes());
        for (final String handlerMethodName : handlerMethodNames) {
            try {
                return targetClass.getMethod(handlerMethodName, handlerParameterTypes);
            } catch (final ReflectiveOperationException e) {
                // Exception handler method signature does not match - Skip this exception handler
            }
        }
        return null;
    }

    private Object[] getExceptionHandlerArgs(final Throwable thrownException, final Object[] originalArgs) {
        final Object[] args = new Object[originalArgs.length + 1];
        args[0] = thrownException;
        System.arraycopy(originalArgs, 0, args, 1, originalArgs.length);
        return args;
    }

    private Class<?>[] getExceptionHandlerParameterTypes(final Class<?> thrownExceptionClass,
            final Class<?>[] methodParameterTypes) {
        final Class<?>[] types = new Class[methodParameterTypes.length + 1];
        types[0] = thrownExceptionClass;
        System.arraycopy(methodParameterTypes, 0, types, 1, methodParameterTypes.length);
        return types;
    }
}
