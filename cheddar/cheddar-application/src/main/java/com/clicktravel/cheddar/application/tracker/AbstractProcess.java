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
package com.clicktravel.cheddar.application.tracker;

public abstract class AbstractProcess implements Process {

    private final ProcessId processId;
    private ProcessStatus status;
    private Long version;

    public AbstractProcess(final ProcessId processId, final ProcessStatus status) {
        this.processId = processId;
        this.status = status;
        version = null;
    }

    public AbstractProcess(final ProcessId processId, final ProcessStatus status, final Long version) {
        this.processId = processId;
        this.status = status;
        this.version = version;
    }

    public void checkStatus(final ProcessStatus expectedStatus) throws InvalidProcessStatusException {
        if (status != expectedStatus) {
            throw new InvalidProcessStatusException(this.getClass(), "Expected status: " + expectedStatus.toString());
        }
    }

    @Override
    public ProcessId processId() {
        return processId;
    }

    @Override
    public ProcessStatus status() {
        return status;
    }

    protected void setStatus(final ProcessStatus status) {
        this.status = status;
    }

    @Override
    public Long version() {
        return version;
    }

    public void setVersion(final Long version) {
        this.version = version;
    }

}
