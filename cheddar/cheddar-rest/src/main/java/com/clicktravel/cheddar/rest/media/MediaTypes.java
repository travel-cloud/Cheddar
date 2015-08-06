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
package com.clicktravel.cheddar.rest.media;

import javax.ws.rs.core.MediaType;

public class MediaTypes {

    public static final String CDM_V1_JSON = "application/vnd.clicktravel.schema-v1+json";

    public static final MediaType CDM_V1_JSON_TYPE = new MediaType("application", "vnd.clicktravel.schema-v1+json");

}
