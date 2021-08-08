/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.frame.api.executive;

import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Identifier for {@link ProcessState}.
 * <p>
 * The requirements are that:
 * <ul>
 * <li>the same {@link ProcessIdentifier} equals itself</li>
 * <li>no two separate {@link ProcessIdentifier} instance equal each other</li>
 * <ul>
 * <p>
 * The easiest way to ensure this is create a new instance each time and allow
 * default {@link Object} equality.
 * <p>
 * Other than the above, the {@link Executive} is free to provide any
 * implementation of this interface as a momento about the {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessIdentifier {
}
