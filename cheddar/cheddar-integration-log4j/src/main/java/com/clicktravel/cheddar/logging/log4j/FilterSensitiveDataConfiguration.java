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
package com.clicktravel.cheddar.logging.log4j;

import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.clicktravel.common.logging.LoggingFilter;

@Component
public class FilterSensitiveDataConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired(required = false)
    private LoggingFilter loggingFilter;

    @Override
    @SuppressWarnings("unchecked")
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (loggingFilter != null) {
            final Logger logger = Logger.getLogger("com.clicktravel.cheddar.server.http.filter.logging");
            final Enumeration<Appender> appenders = logger.getAllAppenders();
            while (appenders.hasMoreElements()) {
                final Appender appender = appenders.nextElement();
                final Layout layout = appender.getLayout();
                appender.setLayout(new MaskSensitiveDataLayout(layout));
            }
        }
    }

    private class MaskSensitiveDataLayout extends Layout {

        private final Layout layout;

        public MaskSensitiveDataLayout(final Layout layout) {
            this.layout = layout;
        }

        @Override
        public String format(final LoggingEvent event) {
            final String formattedLogEntry = layout.format(event);
            String result = formattedLogEntry;
            for (final String matcher : loggingFilter.matchers()) {
                result = result.replaceAll(matcher, loggingFilter.mask());
            }
            result = loggingFilter.matchingFunction().orElse((s) -> s).apply(result);
            return result;
        }

        @Override
        public void activateOptions() {
            layout.activateOptions();
        }

        @Override
        public boolean ignoresThrowable() {
            return layout.ignoresThrowable();
        }
    }
}