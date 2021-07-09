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
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.TraceID;
import com.amazonaws.xray.strategy.sampling.SamplingRequest;
import com.amazonaws.xray.strategy.sampling.SamplingResponse;
import com.amazonaws.xray.strategy.sampling.SamplingStrategy;
import com.clicktravel.cheddar.request.context.AWSXraySegmentContextHolder;
import com.clicktravel.cheddar.request.context.DefaultAWSXraySegmentContext;
import com.clicktravel.cheddar.server.application.configuration.ApplicationConfiguration;

/**
 * Filter used to create an AWS XRay segment for each HTTP request. This then allows any instrumented AWS clients to
 * automatically add sub segments to this segment which will show up in the AWS console for that environment.
 */
@Provider
@Priority(Priorities.USER)
public class AWSXrayRequestFilter implements ContainerRequestFilter {

    final Logger logger = LoggerFactory.getLogger(AWSXrayRequestFilter.class);

    private final boolean awsXrayEnabled;
    private final ApplicationConfiguration applicationConfiguration;

    @Autowired
    public AWSXrayRequestFilter(@Value("${aws.xray.enabled:false}") final boolean awsXrayEnabled,
            final ApplicationConfiguration applicationConfiguration) {
        this.awsXrayEnabled = awsXrayEnabled;
        this.applicationConfiguration = applicationConfiguration;
    }

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        if (awsXrayEnabled == true) {
            try {
                final String segmentName = applicationConfiguration.name();
                final String amazonTraceId = requestContext.getHeaderString("X-Amzn-Trace-Id");

                if (shouldSampleSegment(segmentName, requestContext)) {
                    Segment segment;
                    if (amazonTraceId != null) {
                        final TraceID awsXrayTraceId = TraceID.fromString(amazonTraceId);
                        segment = AWSXRay.beginSegment(segmentName, awsXrayTraceId, null);
                    } else {
                        segment = AWSXRay.beginSegment(segmentName);
                    }
                    addRequestParamsToSegment(segment, requestContext);
                    AWSXraySegmentContextHolder.set(new DefaultAWSXraySegmentContext(segment));
                }
            } catch (final Exception e) {
                logger.debug("Failed to begin XRay segment due to exception " + e.getMessage());
            }
        }
    }

    private boolean shouldSampleSegment(final String segmentName, final ContainerRequestContext requestContext) {
        final SamplingRequest samplingRequest = new SamplingRequest(segmentName,
                requestContext.getUriInfo().getBaseUri().getHost(), requestContext.getUriInfo().getPath(),
                requestContext.getMethod(), requestContext.getHeaderString("Origin"));
        final SamplingStrategy chosenSamplingStrategy = AWSXRay.getGlobalRecorder().getSamplingStrategy();
        // As we have not changed the sampling strategy so XRay should call out to AWS console for settings
        final SamplingResponse sample = chosenSamplingStrategy.shouldTrace(samplingRequest);
        return sample.isSampled();
    }

    private void addRequestParamsToSegment(final Segment segment, final ContainerRequestContext requestContext) {
        final Map<String, Object> requestAttributes = new HashMap<>();
        requestAttributes.put("url", requestContext.getUriInfo().getPath());
        requestAttributes.put("method", requestContext.getMethod());
        segment.putHttp("request", requestAttributes);
    }

}
