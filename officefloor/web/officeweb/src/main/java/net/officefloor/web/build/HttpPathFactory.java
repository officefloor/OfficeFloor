/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.build;

import net.officefloor.server.http.HttpException;

/**
 * Creates the HTTP path.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpPathFactory<T> {

	/**
	 * Obtains the expected type to retrieve values in constructing the path.
	 * 
	 * @return Expected type to retrieve values in constructing the path. May be
	 *         <code>null</code> if no values are required.
	 */
	Class<T> getValuesType();

	/**
	 * <p>
	 * Creates the client application path.
	 * <p>
	 * This is the path on the server to the {@link HttpInput} (i.e. includes
	 * the context path). It, however, does not include <code>protocol</code>,
	 * <code>domain</code> and <code>port</code>.
	 * 
	 * @param values
	 *            Optional object to obtain values to create the path.
	 * @return Application path.
	 * @throws HttpException
	 *             If fails to create the application path.
	 */
	String createApplicationClientPath(T values) throws HttpException;

}
