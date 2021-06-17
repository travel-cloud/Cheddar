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

import java.util.Optional;

import com.amazonaws.xray.entities.Segment;

/**
 * Retains the current XRay segment for the request if one has been set. This needed because 
 * the standard AWSXRayServletFilter is not compatible with CHEDDAR so it needs to be 
 * manually created, stored and closed for each request.
 */
public class AWSXraySegmentContextHolder {

    private final static ThreadLocal<AWSXraySegmentContext> REQUEST_CONTEXT = new ThreadLocal<AWSXraySegmentContext>() {
    };

    public static void set(final AWSXraySegmentContext awsXraySegmentContext) {
        REQUEST_CONTEXT.set(awsXraySegmentContext);
    }

    public static AWSXraySegmentContext get() {
        final AWSXraySegmentContext awsXraySegmentContext = REQUEST_CONTEXT.get();
        return awsXraySegmentContext == null ? NullAWSXraySegmentContext.NULL : awsXraySegmentContext;
    }

    public static void clear() {
        REQUEST_CONTEXT.remove();
    }
    
     private static class NullAWSXraySegmentContext implements AWSXraySegmentContext {

        private static NullAWSXraySegmentContext NULL = new NullAWSXraySegmentContext();

        private NullAWSXraySegmentContext() {
        }

        @Override
        public Optional<Segment> requestSegment() {
            return Optional.empty();
        }

      
    }

}
