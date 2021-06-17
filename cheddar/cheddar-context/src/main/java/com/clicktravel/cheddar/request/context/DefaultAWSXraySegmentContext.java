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
 * Default implementation which stores the AWS XRay Segment context.
 */
public class DefaultAWSXraySegmentContext implements AWSXraySegmentContext {

    private final Optional<Segment> requestSegment;
  
    public DefaultAWSXraySegmentContext(final Segment requestSegment) {
        this.requestSegment = Optional.ofNullable(requestSegment);
    }

    @Override
    public Optional<Segment> requestSegment() {
        return requestSegment;
    }
}
