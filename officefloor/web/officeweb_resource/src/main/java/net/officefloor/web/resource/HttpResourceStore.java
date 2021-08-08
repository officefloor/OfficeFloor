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

package net.officefloor.web.resource;

import java.io.IOException;

/**
 * Store of {@link HttpResource} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResourceStore {

	/**
	 * Obtains the {@link HttpResource}.
	 * 
	 * @param path Path to the {@link HttpResource}.
	 * @return {@link HttpResource}.
	 * @throws IOException If failure in finding the {@link HttpResource}.
	 */
	HttpResource getHttpResource(String path) throws IOException;

	/**
	 * Obtains the default {@link HttpFile} for the {@link HttpDirectory}.
	 * 
	 * @param directory {@link HttpDirectory}.
	 * @return {@link HttpFile} for the {@link HttpDirectory} or <code>null</code>
	 *         if no default {@link HttpFile}.
	 * @throws IOException If failure in obtaining default {@link HttpFile}.
	 */
	HttpFile getDefaultHttpFile(HttpDirectory directory) throws IOException;

}
