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
package com.clicktravel.infrastructure.persistence.inmemory.database;

import static com.clicktravel.common.random.Randoms.randomBoolean;
import static com.clicktravel.common.random.Randoms.randomId;
import static com.clicktravel.common.random.Randoms.randomInt;
import static com.clicktravel.common.random.Randoms.randomString;

import java.util.HashSet;

import org.mockito.internal.util.collections.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryDbDataGenerator {

    static final String UNIT_TEST_SCHEMA_NAME = "unittest";
    static final String STUB_ITEM_TABLE_NAME = "stub_item_" + randomString(10);
    static final String STUB_ITEM_WITH_RANGE_TABLE_NAME = "stub_item_with_range_" + randomString(10);
    static final String STUB_ITEM_WITH_GSI_TABLE_NAME = "stub_item_with_gsi_" + randomString(10);

    public final Logger logger = LoggerFactory.getLogger(this.getClass());

    public StubItem randomStubItem() {
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        stubItem.setStringProperty(randomString(10));
        stubItem.setStringProperty2(randomString(10));
        stubItem.setBooleanProperty(randomBoolean());
        stubItem.setStringSetProperty(new HashSet<>(Sets.newSet(randomString(10), randomString(10), randomString(10))));
        stubItem.setVersion((long) randomInt(100));
        return stubItem;
    }

    public StubItem stubItemWithStringProperty(final String stringProperty) {
        final StubItem stubItem = randomStubItem();
        stubItem.setStringProperty(stringProperty);
        return stubItem;
    }

    public StubItem stubItemWithNullValues() {
        final StubItem stubItem = new StubItem();
        stubItem.setId(randomId());
        stubItem.setVersion((long) randomInt(100));
        return stubItem;
    }

    public StubVariantItem randomStubVariantItem() {
        final StubVariantItem stubItem = new StubVariantItem();
        stubItem.setId(randomId());
        stubItem.setStringProperty(randomString(10));
        stubItem.setStringProperty2(randomString(10));
        stubItem.setVersion((long) randomInt(100));
        return stubItem;
    }

    public StubVariantTwoItem randomStubVariantTwoItem() {
        final StubVariantTwoItem stubItem = new StubVariantTwoItem();
        stubItem.setId(randomId());
        stubItem.setStringProperty(randomString(10));
        stubItem.setStringPropertyTwo(randomString(10));
        stubItem.setVersion((long) randomInt(100));
        return stubItem;
    }

    public StubWithRangeItem randomStubWithRangeItem() {
        final StubWithRangeItem stubItem = new StubWithRangeItem();
        stubItem.setId(randomId());
        stubItem.setSupportingId(randomId());
        stubItem.setStringProperty(randomString(10));
        stubItem.setBooleanProperty(randomBoolean());
        stubItem.setStringSetProperty(new HashSet<>(Sets.newSet(randomString(10), randomString(10), randomString(10))));
        stubItem.setVersion((long) randomInt(100));
        return stubItem;
    }

    public StubWithGlobalSecondaryIndexItem randomStubWithGlobalSecondaryIndexItem() {
        final StubWithGlobalSecondaryIndexItem stubItem = new StubWithGlobalSecondaryIndexItem();
        stubItem.setId(randomId());
        stubItem.setGsiHashProperty(randomString(10));
        stubItem.setGsiRangeProperty(randomInt(100));
        stubItem.setVersion((long) randomInt(100));
        return stubItem;
    }
}
