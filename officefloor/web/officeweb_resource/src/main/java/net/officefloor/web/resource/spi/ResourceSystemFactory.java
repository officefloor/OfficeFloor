/*-
 * #%L
 * Web resources
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

package net.officefloor.web.resource.spi;

import java.io.IOException;

/**
 * Service for the creation of a {@link ResourceSystem}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceSystemFactory {

	/**
	 * <p>
	 * Obtains the protocol name for the created {@link ResourceSystem}.
	 * <p>
	 * The protocol name is used as follows <code>[protocol]:location</code> to
	 * configure a {@link ResourceSystem}.
	 * 
	 * @return Protocol name for the created {@link ResourceSystem}.
	 */
	String getProtocolName();

	/**
	 * Creates the {@link ResourceSystem}.
	 * 
	 * @param context
	 *            {@link ResourceSystemContext}.
	 * @return {@link ResourceSystem}.
	 * @throws IOException
	 *             If fails to create the {@link ResourceSystem}.
	 */
	ResourceSystem createResourceSystem(ResourceSystemContext context) throws IOException;

}
