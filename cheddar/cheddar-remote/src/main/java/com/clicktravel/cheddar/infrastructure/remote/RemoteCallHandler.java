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
package com.clicktravel.cheddar.infrastructure.remote;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetClassAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.clicktravel.common.remote.Asynchronous;
import com.clicktravel.common.remote.AsynchronousExceptionHandler;
import com.clicktravel.common.validation.ValidationException;
import com.clicktravel.cheddar.infrastructure.persistence.database.exception.PersistenceResourceFailureException;
import com.clicktravel.cheddar.remote.FailImmediatelyOnException;
import com.clicktravel.cheddar.request.context.SecurityContextHolder;

/**
 * Handles a remote call by executing the specified method locally and optionally returning the response
 */
@Component
public class RemoteCallHandler {

    /**
     * Delay (in seconds) between a failed call attempt and the next attempt
     */
    private static final int COMMAND_RETRY_DELAY_SECONDS = 2;

    /**
     * Exceptions which always result in immediate failure of a remote call attempt without further retries
     */
    private static final List<Class<? extends Throwable>> alwaysImmediateFailureExceptionClasses = new ArrayList<>();
    {
        alwaysImmediateFailureExceptionClasses.addAll(Arrays.asList(ValidationException.class,
                PersistenceResourceFailureException.class));
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ListableBeanFactory listableBeanFactory;
    private final RemoteCallSender remoteCallSender;
    private final RemoteResponseSender remoteResponseSender;
    private final RemoteCallContextHolder remoteCallContextHolder;
    private final AsynchronousExceptionHandler asynchronousExceptionHandler;
    private final TaggedRemoteCallStatusHolderImpl taggedRemoteCallStatusHolderImpl;

    @Autowired
    public RemoteCallHandler(final ListableBeanFactory listableBeanFactory, final RemoteCallSender remoteCallSender,
            final RemoteResponseSender remoteResponseSender, final RemoteCallContextHolder remoteCallContextHolder,
            final AsynchronousExceptionHandler asynchronousExceptionHandler,
            final TaggedRemoteCallStatusHolderImpl taggedRemoteCallStatusHolderImpl) {
        this.listableBeanFactory = listableBeanFactory;
        this.remoteCallSender = remoteCallSender;
        this.remoteResponseSender = remoteResponseSender;
        this.remoteCallContextHolder = remoteCallContextHolder;
        this.asynchronousExceptionHandler = asynchronousExceptionHandler;
        this.taggedRemoteCallStatusHolderImpl = taggedRemoteCallStatusHolderImpl;
    }

    /**
     * Handles the given {@link RemoteCall} by executing the specified method with the given parameters on a Spring bean
     * that implements the specified interface. If an exception is produced, the method is re-queued to be retried
     * (after a delay) up to a fixed number of attempts. The ultimate response (either a return value or an exception)
     * is optionally returned as a {@link RemoteResponse} by messaging to the originator of the remote call.
     * @param remoteCall Contains details of the method call to perform and where to return the response
     */
    public void handle(final RemoteCall remoteCall) {
        Class<?> interfaceClass = null;
        try {
            interfaceClass = Class.forName(remoteCall.getInterfaceName());
        } catch (final ClassNotFoundException e) {
            throw new RemotingException("Could not find interface class: " + remoteCall.getInterfaceName());
        }

        logger.debug("Attempting remote call; " + remoteCall);
        remoteCall.decrementAttemptsRemaining();
        final Object targetBean = targetBean(interfaceClass);
        final Method interfaceMethod = namedMethod(interfaceClass, remoteCall.getMethodName(),
                remoteCall.getMethodParameterTypes());
        Throwable thrownException = null;
        Object returnValue = null;
        boolean isResponseSuspended;

        try {
            if (remoteCall.hasTag()) {
                taggedRemoteCallStatusHolderImpl.taggedRemoteCallStarted();
            }
            remoteCallContextHolder.initialiseContext(remoteCall);
            SecurityContextHolder.setPrincipal(remoteCall.getPrincipal());
            returnValue = interfaceMethod.invoke(targetBean, remoteCall.getParameters());
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RemotingException("Unable to call remote method: " + e.getMessage());
        } catch (final InvocationTargetException e) {
            thrownException = e.getCause();
        } finally {
            SecurityContextHolder.clearPrincipal();
            isResponseSuspended = remoteCallContextHolder.isResponseSuspended();
            remoteCallContextHolder.clearContext();
            if (remoteCall.hasTag()) {
                taggedRemoteCallStatusHolderImpl.taggedRemoteCallCompleted();
            }
        }

        if (thrownException == null) {
            processCallAttemptSuccess(remoteCall, isResponseSuspended, interfaceMethod, returnValue);
        } else {
            final Method beanMethod = namedMethod(targetClass(targetBean), remoteCall.getMethodName(),
                    remoteCall.getMethodParameterTypes());
            processCallAttemptFailure(remoteCall, isResponseSuspended, interfaceMethod, beanMethod, thrownException);
        }

    }

    private void processCallAttemptSuccess(final RemoteCall remoteCall, final boolean isResponseSuspended,
            final Method interfaceMethod, final Object returnValue) {
        logger.debug("Remote call attempt succeeded; " + remoteCall);
        if (!(isResponseSuspended || isAsynchronousVoid(interfaceMethod))) {
            final RemoteResponse remoteResponse = new RemoteResponse(remoteCall.getCallId());
            remoteResponse.setReturnValue(returnValue);
            remoteResponseSender.sendRemoteResponse(remoteResponse);
        }
    }

    private void processCallAttemptFailure(final RemoteCall remoteCall, final boolean isResponseSuspended,
            final Method interfaceMethod, final Method beanMethod, final Throwable thrownException) {
        final String logMessageDetail = remoteCall + " exception:[" + thrownException.getClass().getName()
                + "] message:[" + thrownException.getMessage() + "]";
        final int attemptsRemaining = remoteCall.getAttemptsRemaining();
        if (shouldRetryMethod(beanMethod, thrownException, attemptsRemaining)) {
            logger.trace("Remote call attempt failed, will retry method call; " + logMessageDetail);
            remoteCallSender.sendDelayedRemoteCall(remoteCall, COMMAND_RETRY_DELAY_SECONDS);
        } else {
            if (attemptsRemaining == 0) {
                logger.debug("Remote call failed all attempts; " + logMessageDetail, thrownException);
            } else {
                logger.debug("Will not retry remote call; " + logMessageDetail);
            }
            if (isAsynchronousVoid(interfaceMethod)) {
                asynchronousExceptionHandler.handle(thrownException);
            } else if (!isResponseSuspended) {
                final RemoteResponse remoteResponse = new RemoteResponse(remoteCall.getCallId());
                remoteResponse.setThrownException(thrownException);
                remoteResponseSender.sendRemoteResponse(remoteResponse);
            }
        }
    }

    /**
     * @param interfaceMethod Method to inspect
     * @return <code>true</code> if the given method is of return type <code>void</code> and is annotated as
     *         <code>@Asynchronous</code>
     */
    private boolean isAsynchronousVoid(final Method interfaceMethod) {
        final boolean isReturnTypeVoid = Void.TYPE.equals(interfaceMethod.getReturnType());
        final boolean hasAsynchronousAnnotation = interfaceMethod.isAnnotationPresent(Asynchronous.class);
        return isReturnTypeVoid && hasAsynchronousAnnotation;
    }

    /**
     * Determine if a remote call method should be retried. This is called in event of an method call attempt that has
     * resulted with a thrown exception.
     * @param beanMethod Method on implementing bean
     * @param thrownException Exception thrown by the bean method
     * @param attemptsRemaining Number of remote call attempts remaining
     * @return <code>true</code> if this method should be retried
     */
    private boolean shouldRetryMethod(final Method beanMethod, final Throwable thrownException,
            final int attemptsRemaining) {
        if (attemptsRemaining == 0) {
            return false;
        }
        final List<Class<? extends Throwable>> immediateFailureExceptionClasses = new ArrayList<>(
                alwaysImmediateFailureExceptionClasses);
        final FailImmediatelyOnException annotation = beanMethod.getAnnotation(FailImmediatelyOnException.class);
        if (annotation != null && annotation.value() != null) {
            immediateFailureExceptionClasses.addAll(Arrays.asList(annotation.value()));
        }
        for (final Class<? extends Throwable> immediateFailureExceptionClass : immediateFailureExceptionClasses) {
            if (immediateFailureExceptionClass.isAssignableFrom(thrownException.getClass())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the {@link Method} on the specified interface, identified by name and parameter types
     * @param clazz Name of interface or implementing bean containing the sought method
     * @param methodName Name of the method
     * @param methodParameterTypes Class names of the method's formal parameters
     * @return target method
     */
    private Method namedMethod(final Class<?> clazz, final String methodName, final String[] methodParameterTypes) {
        try {
            final List<Class<?>> parameterClassesList = new ArrayList<>();
            for (final String methodParameterType : methodParameterTypes) {
                final Class<?> parameterClass = ClassUtils.forName(methodParameterType, getClass().getClassLoader());
                parameterClassesList.add(parameterClass);
            }
            final Class<?>[] parameterClassesArray = parameterClassesList.toArray(new Class<?>[0]);
            final Method method = clazz.getMethod(methodName, parameterClassesArray);
            return method;
        } catch (final Exception e) {
            throw new RemotingException("Could not locate method [" + methodName + "] on bean with interface ["
                    + clazz.getName() + "]", e);
        }
    }

    /**
     * Find target bean that implements the specified interface. The bean name should begin with "default" TODO Devise a
     * more robust way of identifying target beans
     * @param interfaceClass
     * @return target bean that implements the specified interface
     */
    private Object targetBean(final Class<?> interfaceClass) {
        final Map<String, ?> candidateBeanMap = listableBeanFactory.getBeansOfType(interfaceClass);

        // If exactly one bean is available, assume it is the target bean irrespective of annotations
        if (candidateBeanMap.entrySet().size() == 1) {
            return candidateBeanMap.values().iterator().next();
        }

        // Pick any bean that has name beginning "default"
        for (final Entry<String, ?> candidateBeanMapEntry : candidateBeanMap.entrySet()) {
            final String beanName = candidateBeanMapEntry.getKey();
            if (beanName.startsWith("default")) {
                return candidateBeanMapEntry.getValue();
            }
        }
        throw new RemotingException("Could not find default implementation for interface [" + interfaceClass.getName()
                + "]");
    }

    /**
     * Returns the class of the specified bean. If the bean is a proxy of the real bean, the class of the real bean is
     * returned. This is intended to allow inspection of the bean target class (e.g. search for annotations on bean
     * implementation class), whether or not the bean has been proxied by Spring AOP.
     * @param bean Bean to find target class for
     * @return Class of bean
     */
    private Class<?> targetClass(final Object bean) {
        return bean instanceof TargetClassAware ? ((TargetClassAware) bean).getTargetClass() : bean.getClass();
    }
}
