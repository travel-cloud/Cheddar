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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.system.event.SystemEvent;
import com.clicktravel.cheddar.system.event.handler.AbstractSystemEventHandler;
import com.thoughtworks.xstream.XStream;

/**
 * Handles a {@link PendingResultOfferedEvent} by offering the {@link Result} to the specified {@link PendingResult} if it
 * is stored on this application instance.
 */
@Component
public class PendingResultOfferedEventHandler extends AbstractSystemEventHandler {

    private final PendingResultsHolder pendingResultsHolder;
    private final XStream xStream = new XStream();

    @Autowired
    public PendingResultOfferedEventHandler(@Value("${server.application.name}") final String applicationName,
            @Value("${server.application.version}") final String applicationVersion,
            final PendingResultsHolder pendingResultsHolder) {
        super(applicationName, applicationVersion);
        this.pendingResultsHolder = pendingResultsHolder;
    }

    @Override
    protected void handleSystemEvent(final SystemEvent systemEvent) {
        final PendingResultOfferedEvent event = (PendingResultOfferedEvent) systemEvent;
        final PendingResult pendingResult = pendingResultsHolder.get(event.getPendingResultId());
        if (pendingResult != null) {
            final Result result = (Result) xStream.fromXML(event.getResultXml());
            pendingResult.offerResult(result);
        }
    }

    @Override
    public Class<? extends SystemEvent> getEventClass() {
        return PendingResultOfferedEvent.class;
    }

}
