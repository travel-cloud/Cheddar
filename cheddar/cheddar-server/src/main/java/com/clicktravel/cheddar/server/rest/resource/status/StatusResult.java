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

public class StatusResult {

    private String name;
    private String version;
    private String frameworkVersion;
    private String status;
    private String javaVersion;
    private String javaVendor;
    private boolean processingRestRequest;
    private boolean deferrableProcessing;
    private MaximumWorkRates maximumWorkRates;

    // TODO Remove the "processedRecentDeferrableEvent" property and getter/setter when deployment scripts have been
    // updated (property has been renamed deferrableProcessing)
    @SuppressWarnings("unused")
    private boolean processedRecentDeferrableEvent;

    public boolean isProcessedRecentDeferrableEvent() {
        return deferrableProcessing;
    }

    public void setProcessedRecentDeferrableEvent(final boolean processedRecentDeferrableEvent) {
        this.processedRecentDeferrableEvent = deferrableProcessing;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getFrameworkVersion() {
        return frameworkVersion;
    }

    public void setFrameworkVersion(final String frameworkVersion) {
        this.frameworkVersion = frameworkVersion;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(final String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getJavaVendor() {
        return javaVendor;
    }

    public void setJavaVendor(final String javaVendor) {
        this.javaVendor = javaVendor;
    }

    public boolean isProcessingRestRequest() {
        return processingRestRequest;
    }

    public void setProcessingRestRequest(final boolean processingRestRequest) {
        this.processingRestRequest = processingRestRequest;
    }

    public boolean isDeferrableProcessing() {
        return deferrableProcessing;
    }

    public void setDeferrableProcessing(final boolean deferrableProcessing) {
        this.deferrableProcessing = deferrableProcessing;
    }

    public MaximumWorkRates getMaximumWorkRates() {
        return maximumWorkRates;
    }

    public void setMaximumWorkRates(final MaximumWorkRates maximumWorkRates) {
        this.maximumWorkRates = maximumWorkRates;
    }

}
