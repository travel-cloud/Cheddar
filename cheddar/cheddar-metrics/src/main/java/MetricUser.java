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

public class MetricUser {

    private final String id;
    private final String organisationId;
    private final String name;

    public MetricUser(final String id, final String organisationId, final String name) {
        super();
        this.id = id;
        this.organisationId = organisationId;
        this.name = name;
    }

    public String id() {
        return id;
    }

    public String organisationId() {
        return organisationId;
    }

    public String name() {
        return name;
    }

}
