/*-
 * #%L
 * Composition
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

package net.officefloor.activity.compose.build;

import net.officefloor.activity.compose.ComposeConfiguration;

/**
 * Source to build composition.
 */
public interface ComposeSource<T, C extends ComposeConfiguration> {

    /**
     * Sources the item from the composition.
     *
     * @param context {@link ComposeContext}.
     * @return Item from composition.
     * @throws Exception If fails to source item.
     */
    T source(ComposeContext<C> context) throws Exception;

}
