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
package com.clicktravel.cheddar.system.event;

/**
 * System event to state that the maximum rate of work has been set.
 */
public class MaximumWorkRateSetEvent extends AbstractSystemEvent {

    private int restRequestBucketCapacity;
    private int restRequestTokenReplacementDelay;
    private int highPriorityEventHandlerBucketCapacity;
    private int highPriorityEventHandlerTokenReplacementDelay;
    private int lowPriorityEventHandlerBucketCapacity;
    private int lowPriorityEventHandlerTokenReplacementDelay;

    public int getRestRequestBucketCapacity() {
        return restRequestBucketCapacity;
    }

    public void setRestRequestBucketCapacity(final int restRequestBucketCapacity) {
        this.restRequestBucketCapacity = restRequestBucketCapacity;
    }

    public int getRestRequestTokenReplacementDelay() {
        return restRequestTokenReplacementDelay;
    }

    public void setRestRequestTokenReplacementDelay(final int restRequestTokenReplacementDelay) {
        this.restRequestTokenReplacementDelay = restRequestTokenReplacementDelay;
    }

    public int getHighPriorityEventHandlerBucketCapacity() {
        return highPriorityEventHandlerBucketCapacity;
    }

    public void setHighPriorityEventHandlerBucketCapacity(final int highPriorityEventHandlerBucketCapacity) {
        this.highPriorityEventHandlerBucketCapacity = highPriorityEventHandlerBucketCapacity;
    }

    public int getHighPriorityEventHandlerTokenReplacementDelay() {
        return highPriorityEventHandlerTokenReplacementDelay;
    }

    public void setHighPriorityEventHandlerTokenReplacementDelay(final int highPriorityEventHandlerTokenReplacementDelay) {
        this.highPriorityEventHandlerTokenReplacementDelay = highPriorityEventHandlerTokenReplacementDelay;
    }

    public int getLowPriorityEventHandlerBucketCapacity() {
        return lowPriorityEventHandlerBucketCapacity;
    }

    public void setLowPriorityEventHandlerBucketCapacity(final int lowPriorityEventHandlerBucketCapacity) {
        this.lowPriorityEventHandlerBucketCapacity = lowPriorityEventHandlerBucketCapacity;
    }

    public int getLowPriorityEventHandlerTokenReplacementDelay() {
        return lowPriorityEventHandlerTokenReplacementDelay;
    }

    public void setLowPriorityEventHandlerTokenReplacementDelay(final int lowPriorityEventHandlerTokenReplacementDelay) {
        this.lowPriorityEventHandlerTokenReplacementDelay = lowPriorityEventHandlerTokenReplacementDelay;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + highPriorityEventHandlerBucketCapacity;
        result = prime * result + highPriorityEventHandlerTokenReplacementDelay;
        result = prime * result + lowPriorityEventHandlerBucketCapacity;
        result = prime * result + lowPriorityEventHandlerTokenReplacementDelay;
        result = prime * result + restRequestBucketCapacity;
        result = prime * result + restRequestTokenReplacementDelay;
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MaximumWorkRateSetEvent other = (MaximumWorkRateSetEvent) obj;
        if (highPriorityEventHandlerBucketCapacity != other.highPriorityEventHandlerBucketCapacity) {
            return false;
        }
        if (highPriorityEventHandlerTokenReplacementDelay != other.highPriorityEventHandlerTokenReplacementDelay) {
            return false;
        }
        if (lowPriorityEventHandlerBucketCapacity != other.lowPriorityEventHandlerBucketCapacity) {
            return false;
        }
        if (lowPriorityEventHandlerTokenReplacementDelay != other.lowPriorityEventHandlerTokenReplacementDelay) {
            return false;
        }
        if (restRequestBucketCapacity != other.restRequestBucketCapacity) {
            return false;
        }
        if (restRequestTokenReplacementDelay != other.restRequestTokenReplacementDelay) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MaximumWorkRateSetEvent [restRequestBucketCapacity=").append(restRequestBucketCapacity)
                .append(", restRequestTokenReplacementDelay=").append(restRequestTokenReplacementDelay)
                .append(", highPriorityEventHandlerBucketCapacity=").append(highPriorityEventHandlerBucketCapacity)
                .append(", highPriorityEventHandlerTokenReplacementDelay=")
                .append(highPriorityEventHandlerTokenReplacementDelay)
                .append(", lowPriorityEventHandlerBucketCapacity=").append(lowPriorityEventHandlerBucketCapacity)
                .append(", lowPriorityEventHandlerTokenReplacementDelay=")
                .append(lowPriorityEventHandlerTokenReplacementDelay).append("]");
        return builder.toString();
    }

}
