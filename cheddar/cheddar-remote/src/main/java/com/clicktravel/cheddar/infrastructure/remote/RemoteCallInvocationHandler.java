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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.request.context.SecurityContextHolder;
import com.clicktravel.common.remote.Asynchronous;

@Component
public class RemoteCallInvocationHandler implements InvocationHandler {

    private final Map<Object, String> proxyToInterfaceNameMap = new HashMap<>();
    private final RemotingGateway remotingGateway;
    private final RemoteCallTagLogic remoteCallTagLogic;

    @Autowired
    public RemoteCallInvocationHandler(final RemotingGateway remotingGateway,
            final RemoteCallTagLogic remoteCallTagLogic) {
        this.remotingGateway = remotingGateway;
        this.remoteCallTagLogic = remoteCallTagLogic;
    }

    public <T> T createProxy(final Class<T> remoteInterface) {
        final Object proxy = Proxy.newProxyInstance(remoteInterface.getClassLoader(), new Class[] { remoteInterface },
                this);
        proxyToInterfaceNameMap.put(proxy, remoteInterface.getName());
        return remoteInterface.cast(proxy);
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(this, args);
        } else {
            return invokeRemote(proxy, method, args);
        }
    }

    private Object invokeRemote(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Object returnValue = null;
        final RemoteCall remoteCall = buildRemoteCall(proxy, method, args);
        if (shouldReceiveResponse(method)) {
            returnValue = remotingGateway.invokeSynchronously(remoteCall);
        } else {
            remotingGateway.invokeAsynchronouslyWithoutResponse(remoteCall);
        }
        return returnValue;
    }

    private RemoteCall buildRemoteCall(final Object proxy, final Method method, final Object[] args) {
        final List<String> methodParameterTypesList = new ArrayList<>();
        for (final Class<?> parameterClass : method.getParameterTypes()) {
            methodParameterTypesList.add(parameterClass.getName());
        }
        final String[] methodParameterTypesArray = methodParameterTypesList.toArray(new String[0]);
        final String principal = SecurityContextHolder.getPrincipal();
        return new RemoteCall(proxyToInterfaceNameMap.get(proxy), method.getName(), methodParameterTypesArray, args,
                principal, remoteCallTagLogic.shouldTagRemoteCall());
    }

    private boolean shouldReceiveResponse(final Method method) {
        final boolean isReturnTypeVoid = Void.TYPE.equals(method.getReturnType());
        final boolean hasAsynchronousAnnotation = method.isAnnotationPresent(Asynchronous.class);
        return !(isReturnTypeVoid && hasAsynchronousAnnotation);
    }

}
