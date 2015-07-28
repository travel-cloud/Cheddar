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
package com.clicktravel.cheddar.application.pending.result;

import static com.clicktravel.common.random.Randoms.randomId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Simple threadsafe map of locally stored {@link PendingResult} objects, keyed by their ID
 */
@Component
class PendingResultsHolder {

    private final Map<String, PendingResult> pendingResults = new ConcurrentHashMap<>();

    public String create() {
        final String id = randomId();
        pendingResults.put(id, new PendingResult());
        return id;
    }

    public void remove(final String id) {
        pendingResults.remove(id);
    }

    public PendingResult get(final String id) {
        return pendingResults.get(id);
    }

}
