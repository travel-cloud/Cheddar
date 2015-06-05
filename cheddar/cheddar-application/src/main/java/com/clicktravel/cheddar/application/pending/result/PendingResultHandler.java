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

import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.clicktravel.cheddar.system.event.publisher.SystemEventPublisher;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;

/**
 * Facade for using {@link PendingResult} objects. This facade has methods to create and poll a {@link PendingResult}
 * object for a value which is supplied by another thread which may be executing in a different (remote) application
 * instance. Each {@link PendingResult} has a unique ID generated on creation which is used to coordinate its operation.
 */
@Component
public class PendingResultHandler {

    private final PendingResultsHolder pendingResultsHolder;
    private final String applicationName;
    private final XStream xStream = new XStream();

    @Autowired
    public PendingResultHandler(final PendingResultsHolder pendingResultsHolder,
            @Value("${server.application.name}") final String applicationName) {
        this.pendingResultsHolder = pendingResultsHolder;
        this.applicationName = applicationName;
    }

    /**
     * Creates a {@link PendingResult} stored on the local application instance
     * @return pendingResultId Unique identifier of created {@PendingResult}
     */
    public String createPendingResult() {
        return pendingResultsHolder.create();
    }

    /**
     * Removes a {@link PendingResult} stored on the local application instance
     * @param pendingResultId ID of {@link PendingResult} to remove
     */
    public void removePendingResult(final String pendingResultId) {
        pendingResultsHolder.remove(pendingResultId);
    }

    /**
     * Polls a {@link PendingResult} stored on the local application instance for a returned value, blocking until a
     * value is offered or an exception is thrown
     * @param pendingResultId ID of locally stored {@link PendingResult}
     * @return Value from returned result
     * @throws Exception If exception was thrown
     */
    public Object pollValue(final String pendingResultId) throws Exception {
        final PendingResult pendingResult = pendingResultsHolder.get(pendingResultId);
        return pendingResult.pollResult().getValue();
    }

    /**
     * Offers a return value for a {@link PendingResult}, which is possibly stored on a different (remote) application
     * instance
     * @param pendingResultId ID of {@link PendingResult} to offer value to
     * @param value Value to offer
     */
    public void offerValue(final String pendingResultId, final Object value) {
        offerResult(pendingResultId, new SimpleResult(value));
    }

    /**
     * Offers a thrown exception for a {@link PendingResult}, which is possibly stored on a different (remote)
     * application instance
     * @param pendingResultId of {@link PendingResult} to offer thrown exception to
     * @param exception Thrown exception to offer
     */
    public void offerException(final String pendingResultId, final Exception exception) {
        offerResult(pendingResultId, new ExceptionResult(exception));
    }

    private void offerResult(final String pendingResultId, final Result result) {
        final PendingResultSetEvent event = new PendingResultSetEvent();
        event.setTargetApplicationName(applicationName);
        event.setPendingResultId(pendingResultId);
        event.setResultXml(toCompactXml(result));
        SystemEventPublisher.instance().publishEvent(event);
    }

    private String toCompactXml(final Object object) {
        final StringWriter stringWriter = new StringWriter();
        xStream.marshal(object, new CompactWriter(stringWriter));
        return stringWriter.toString();
    }
}
