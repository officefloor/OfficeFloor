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

import java.io.IOException;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * Provides ability to send an {@link Object} response.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpObjectResponder<T> {

	/**
	 * Obtains the <code>Content-Type</code> provided by this
	 * {@link HttpObjectResponder}.
	 * 
	 * @return <code>Content-Type</code> provided by this
	 *         {@link HttpObjectResponder}.
	 */
	String getContentType();

	/**
	 * Obtains the object type expected for this {@link HttpObjectResponder}.
	 * 
	 * @return Type of object expected for this {@link HttpObjectResponder}.
	 */
	Class<T> getObjectType();

	/**
	 * Sends the object.
	 * 
	 * @param object
	 *            Object to send.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @throws IOException
	 *             If fails to send the object.
	 */
	void send(T object, ServerHttpConnection connection) throws IOException;

}
