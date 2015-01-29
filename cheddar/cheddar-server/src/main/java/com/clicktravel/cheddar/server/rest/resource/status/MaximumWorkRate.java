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
package com.clicktravel.cheddar.server.rest.resource.status;

public class MaximumWorkRate {

    private int bucketCapacity;
    private long tokenReplacementDelay;

    public int getBucketCapacity() {
        return bucketCapacity;
    }

    public void setBucketCapacity(final int bucketCapacity) {
        this.bucketCapacity = bucketCapacity;
    }

    public void setTokenReplacementDelay(final long tokenReplacementDelay) {
        this.tokenReplacementDelay = tokenReplacementDelay;
    }

    public long getTokenReplacementDelay() {
        return tokenReplacementDelay;
    }

}
