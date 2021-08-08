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

package net.officefloor.web.state;

import java.io.Serializable;
import java.util.Iterator;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.value.load.ValueLoader;

/**
 * State for the {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestState {

	/**
	 * Loads values from the {@link HttpRequest}.
	 * 
	 * @param valueLoader
	 *            {@link ValueLoader} to receive the values.
	 * @throws HttpException
	 *             if fails to load values.
	 */
	void loadValues(ValueLoader valueLoader) throws HttpException;

	/**
	 * Obtains the {@link Object} that is bound to the name.
	 * 
	 * @param name
	 *            Name.
	 * @return {@link Object} bound to the name or <code>null</code> if no
	 *         {@link Object} bound by the name.
	 */
	Serializable getAttribute(String name);

	/**
	 * Obtains an {@link Iterator} to the names of the bound {@link Object}
	 * instances.
	 * 
	 * @return {@link Iterator} to the names of the bound {@link Object}
	 *         instances.
	 */
	Iterator<String> getAttributeNames();

	/**
	 * Binds the {@link Object} to the name.
	 * 
	 * @param name
	 *            Name.
	 * @param object
	 *            {@link Object}. Must be {@link Serializable} as this
	 *            {@link HttpRequestState} may be stored in the
	 *            {@link HttpSession} to maintain its state across a redirect.
	 */
	void setAttribute(String name, Serializable object);

	/**
	 * Removes the bound {@link Object} by the name.
	 * 
	 * @param name
	 *            Name of bound {@link Object} to remove.
	 */
	void removeAttribute(String name);

}
