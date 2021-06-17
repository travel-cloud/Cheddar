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
import java.util.List;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.uri.UriTemplate;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.entities.Segment;
import com.clicktravel.cheddar.request.context.AWSXraySegmentContextHolder;
import com.clicktravel.cheddar.request.context.DefaultAWSXraySegmentContext;

/**
 * Filter used to create an AWS XRay segment for each HTTP request. This then allows any instrumented AWS clients to
 * automatically add sub segments to this segment which will show up in the AWS console for that environment.
 */
@Provider
@Priority(Priorities.USER)
public class AWSXrayRequestFilter implements ContainerRequestFilter {

    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {
        String segmentName;
        if (requestContext.getUriInfo() instanceof ExtendedUriInfo) {
            segmentName = getPathWithTemplateParamsNotValues(requestContext.getMethod(),
                    (ExtendedUriInfo) requestContext.getUriInfo());
        } else {
            // This is in case we ever swap the ContainerRequestFilter strategy
            // and ExtendedUriInfo is no longer returned with getUriInfo()
            segmentName = requestContext.getMethod() + " " + requestContext.getUriInfo().getPath();
        }
        final Segment segment = AWSXRay.beginSegment(segmentName);
        AWSXraySegmentContextHolder.set(new DefaultAWSXraySegmentContext(segment));
    }

    /**
     * In order to better group the XRay segments we would like name them after the request's path without its template
     * params substituted for the real values and no braces e.g GET wire/id/queue.
     *
     * @param requestMethod the string verb used for the request e.g. POST
     * @param requestUriInfo for the current request
     * @return A String that represents the path with template params.
     */
    private String getPathWithTemplateParamsNotValues(final String requestMethod,
            final ExtendedUriInfo requestUriInfo) {
        final List<UriTemplate> matchedTemplates = requestUriInfo.getMatchedTemplates();
        final StringBuilder builder = new StringBuilder();

        builder.append(requestMethod);
        builder.append(" ");

        for (int i = matchedTemplates.size() - 1; i >= 0; i--) {
            builder.append(matchedTemplates.get(i).getTemplate().replaceAll("\\{", "").replaceAll("\\}", ""));
        }

        return builder.toString();
    }

}
