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
package com.clicktravel.cheddar.infrastructure.persistence.cache;

public interface ItemCache {

    /**
     * Gets value from the cache but gives up after your given timeout in seconds
     * 
     * @param key
     * @param timeout
     * @return the item stored in the cache
     */
    Object getItem(String key, long timeout);

    /**
     * Saves object to the cache with a given value that will expire in a given number of seconds
     * @param key to store against the object
     * @param expire time in seconds
     * @param item seriaziable object to store in the cache
     */
    void putItem(String key, Object item, long expire);

}
