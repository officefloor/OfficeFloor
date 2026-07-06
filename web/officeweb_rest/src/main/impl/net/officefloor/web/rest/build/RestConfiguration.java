/*-
 * #%L
 * Web REST
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.rest.build;

/**
 * Obtains the configuration item.
 */
public interface RestConfiguration {

    /**
     * Obtains the configuration item.
     *
     * @param itemName Item name.
     * @param type     Type of configuration.
     * @param <T>      Type of configuration.
     * @return Configuration or <code>null</code> if not configured or invalid.
     */
    <T> T getConfiguration(String itemName, Class<T> type);

}
