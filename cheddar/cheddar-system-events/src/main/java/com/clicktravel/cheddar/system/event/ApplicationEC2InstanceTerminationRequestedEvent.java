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

/*
 * System event to indicate that a termination request has been received for an EC2 instance of an application
 */
public class ApplicationEC2InstanceTerminationRequestedEvent extends AbstractSystemEvent {

    private String ec2InstanceId;

    public String getEc2InstanceId() {
        return ec2InstanceId;
    }

    public void setEc2InstanceId(final String ec2InstanceId) {
        this.ec2InstanceId = ec2InstanceId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ec2InstanceId == null) ? 0 : ec2InstanceId.hashCode());
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
        final ApplicationEC2InstanceTerminationRequestedEvent other = (ApplicationEC2InstanceTerminationRequestedEvent) obj;
        if (ec2InstanceId == null) {
            if (other.ec2InstanceId != null) {
                return false;
            }
        } else if (!ec2InstanceId.equals(other.ec2InstanceId)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "ApplicationEC2InstanceTerminationRequestedEvent [ec2InstanceId=" + ec2InstanceId
                + ", getEc2InstanceId()=" + getEc2InstanceId() + ", type()=" + type() + ", getTargetApplicationName()="
                + getTargetApplicationName() + ", getTargetApplicationVersion()=" + getTargetApplicationVersion() + "]";
    }

}
