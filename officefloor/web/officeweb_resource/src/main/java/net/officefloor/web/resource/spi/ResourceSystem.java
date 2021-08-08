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
import java.nio.file.Path;

import net.officefloor.web.resource.HttpResourceStore;

/**
 * Underlying system to the {@link HttpResourceStore}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceSystem {

	/**
	 * Obtains the {@link Path} to the resource.
	 * 
	 * @param path
	 *            Path for the resource.
	 * @return {@link Path} if resource found, otherwise <code>null</code>.
	 * @throws IOException
	 *             If failure in obtaining {@link Path}.
	 */
	Path getResource(String path) throws IOException;

}
