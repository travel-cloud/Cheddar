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
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import com.clicktravel.cheddar.request.context.AWSXraySegmentContextHolder;

/**
 * The response filter that will be called at the end of each HTTP response for all services. If there is an open AWS
 * XRay segment in the context holder it will grab it and close it then clear down the context holder for this thread.
 */
@Provider
@Priority(Priorities.USER)
public class AWSXrayResponseFilter implements ContainerResponseFilter {

    final private boolean awsXrayEnabled;

    @Autowired
    public AWSXrayResponseFilter(@Value("${aws.xray.enabled:false}") final boolean awsXrayEnabled) {
        this.awsXrayEnabled = awsXrayEnabled;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
            throws IOException {
        if (awsXrayEnabled == true) {
            if (AWSXraySegmentContextHolder.get().requestSegment().isPresent()) {
                final Segment requestSegment = AWSXraySegmentContextHolder.get().requestSegment().get();
                sendResponseAttributesForSegment(responseContext.getStatus(), requestSegment);
                final Segment segmentWithResponseState = setSegmentStateBasedOnResponseCode(responseContext.getStatus(),
                        requestSegment);
                AWSXRay.getGlobalRecorder().setTraceEntity(segmentWithResponseState);
                AWSXRay.endSegment();
            }
            AWSXraySegmentContextHolder.clear();
        }
    }

    private void sendResponseAttributesForSegment(final int responseCode, final Segment segment) {
        final Map<String, Object> responseAttributes = new HashMap<>();
        responseAttributes.put("status", responseCode);
        segment.putHttp("response", responseAttributes);
    }

    private Segment setSegmentStateBasedOnResponseCode(final int responseCode, final Segment segment) {
        switch (responseCode / 100) {
            case 4:
                segment.setError(true);
                if (responseCode == 429) {
                    segment.setThrottle(true);
                }
                break;
            case 5:
                segment.setFault(true);
                break;
            default:
                break;
        }
        return segment;
    }
}
