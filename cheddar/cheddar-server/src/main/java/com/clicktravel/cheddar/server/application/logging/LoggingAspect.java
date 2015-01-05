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
package com.clicktravel.cheddar.server.application.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@formatter:off
/**
 * Aspect for automatically logging method calls. The methods to log are identified by a {@code @Pointcut} definition.
 * A log message at DEBUG level is written when the logged method completes. The message includes class and name of
 * method, call parameters, returned value (or exception) and method execution time.</br>
 * To use this class, define a derived class as follows:
 * <pre>
 * {@code @Aspect}
 * {@code @Order(50)}
 * {@code @Component}
 * public class MyLoggingAspect extends LoggingAspect
 *     {@code @Override}
 *     {@code @Pointcut("myPointcut")}
 *     public void loggable() {}
 * }
 * </pre>
 */
//@formatter:on
public abstract class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public abstract void loggable();

    @Around("loggable()")
    protected Object advice(final ProceedingJoinPoint point) throws Throwable {
        // If not logging, avoid overhead of building expressions in logging statements
        return logger.isDebugEnabled() ? proceedAndLog(point) : point.proceed();
    }

    private Object proceedAndLog(final ProceedingJoinPoint point) throws Throwable {
        final String methodCallDetail = methodCallDetail(point);
        final long startTime = System.currentTimeMillis();
        try {
            final Object result = point.proceed();
            logger.debug("Method call; call:[{}] return:[{}] timeMillis:[{}]", methodCallDetail, result,
                    System.currentTimeMillis() - startTime);
            return result;
        } catch (final Throwable e) {
            logger.debug("Method call; call:[{}] exception:[{}] timeMillis:[{}]", methodCallDetail, e,
                    System.currentTimeMillis() - startTime);
            throw e;
        }
    }

    private String methodCallDetail(final ProceedingJoinPoint point) {
        final StringBuilder sb = new StringBuilder();
        sb.append(point.getTarget().getClass().getSimpleName());
        sb.append('.');
        sb.append(((MethodSignature) (point.getSignature())).getMethod().getName());
        sb.append('(');
        boolean firstArg = true;
        for (final Object arg : point.getArgs()) {
            if (!firstArg) {
                sb.append(',');
            }
            firstArg = false;
            sb.append(arg == null ? "null" : arg.toString());
        }
        sb.append(')');
        return sb.toString();
    }
}
