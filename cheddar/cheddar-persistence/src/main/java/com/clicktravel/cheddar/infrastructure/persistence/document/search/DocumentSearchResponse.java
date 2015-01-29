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
package com.clicktravel.cheddar.infrastructure.persistence.document.search;

import java.util.List;

public class DocumentSearchResponse<T extends Document> {

    private final int totalCount;
    private final String cursor;
    private final List<T> hits;

    public DocumentSearchResponse(final int totalCount, final String cursor, final List<T> hits) {
        this.totalCount = totalCount;
        this.cursor = cursor;
        this.hits = hits;
    }

    public int getCount() {
        if (hits != null) {
            return hits.size();
        }

        return 0;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public String getCursor() {
        return cursor;
    }

    public List<T> getHits() {
        return hits;
    }
}
