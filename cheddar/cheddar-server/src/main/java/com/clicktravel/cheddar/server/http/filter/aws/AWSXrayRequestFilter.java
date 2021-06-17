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
package com.clicktravel.cheddar.server.http.filter.aws;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import com.clicktravel.cheddar.request.context.AWSXraySegmentContextHolder;
import com.clicktravel.cheddar.request.context.DefaultAWSXraySegmentContext;

/**
 *  Filter used to create an AWS XRay segment for each HTTP request. This then allows
 *  any instrumented AWS clients to automatically add sub segments to this segment which 
 *  will show up in the AWS console for that environment.
 */
@Provider
@Priority(Priorities.USER)
public class AWSXrayRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        final Segment segment = AWSXRay.beginSegment("Starting request segment for:" + requestContext.getUriInfo().getPath());
        AWSXraySegmentContextHolder.set(new DefaultAWSXraySegmentContext(segment));
    }

}
