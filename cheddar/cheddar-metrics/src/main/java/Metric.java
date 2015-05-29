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

import java.util.HashMap;
import java.util.Map;

public class Metric {

    private final String userId;
    private final String name;
    private final Map<String, Object> metaData;

    public Metric(final String userId, final String name) {
        this.userId = userId;
        this.name = name;
        metaData = new HashMap<>();
    }

    public void addMetric(final String key, final Object value) {
        metaData.put(key, value);
    }

    public String userId() {
        return userId;
    }

    public String name() {
        return name;
    }

    public Map<String, Object> metaData() {
        return metaData;
    }

}
