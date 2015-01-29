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
package com.clicktravel.infrastructure.host.aws.ec2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clicktravel.common.http.client.HttpClient;

public class Ec2InstanceDataAccessor implements Ec2InstanceData {

    private static String METADATA_PATH = "/meta-data";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HttpClient client;
    private String instanceId;

    /**
     * @param client HttpClient with base URI configured to access a particular EC2 instance data
     */
    public Ec2InstanceDataAccessor(final HttpClient client) {
        this.client = client;
    }

    @Override
    public String getInstanceId() {
        if (instanceId == null) {
            logger.debug("About to retrieve EC2 instance id");
            instanceId = readMetadata("/instance-id");
            logger.debug("Retrieved EC2 instance id: [" + instanceId + "]");
        }
        return instanceId;
    }

    private String readMetadata(final String propertyName) {
        return client.get(METADATA_PATH + propertyName).readEntity(String.class);
    }
}
