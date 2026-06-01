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
 * Context for the REST path.
 */
public interface RestPathContext {

    /**
     * Obtains the path.
     *
     * @return Path.
     */
    String getPath();

    /**
     * Obtains the parent {@link RestPathContext}.
     *
     * @return Parent {@link RestPathContext} or <code>null</code> if root path.
     */
    RestPathContext getParentPath();

    /**
     * <p>
     * Obtains additional configuration for the REST path.
     * <p>
     * This for example is CORS specific configuration for the REST path.
     *
     * @param itemName Name of configuration item.
     * @param type     Type of configuration.
     * @param <T>      Type of configuration.
     * @return Configuration item.
     */
    <T> T getConfiguration(String itemName, Class<T> type);

}
