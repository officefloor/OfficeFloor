/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.state;

import java.util.Iterator;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * State for the web application instance.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpApplicationState {

	/**
	 * Obtains the context path for the application.
	 * 
	 * @return Context path for the application.
	 */
	String getContextPath();

	/**
	 * <p>
	 * Creates the client URL for this application.
	 * <p>
	 * This includes <code>protocol</code>, <code>domain</code> and
	 * <code>port</code>.
	 * 
	 * @param isSecure
	 *            Indicates if the URL is secure.
	 * @param path
	 *            Path including query string and fragment for the URL.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @return Client URL for the application.
	 */
	String createApplicationClientUrl(boolean isSecure, String path, ServerHttpConnection connection);

	/**
	 * <p>
	 * Creates the client path for this application.
	 * <p>
	 * This is the public path on the server, and does NOT
	 * <code>protocol</code>, <code>domain</code> nor <code>port</code>.
	 * 
	 * @param path
	 *            Path including query string and fragment for the path.
	 * @return Client path for the application.
	 */
	String createApplicationClientPath(String path);

	/**
	 * Extracts the application path from the {@link HttpRequest}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @return Application path.
	 * @throws HttpException
	 *             If invalid path for this application.
	 */
	String extractApplicationPath(ServerHttpConnection connection) throws HttpException;

	/**
	 * Obtains the {@link Object} that is bound to the name.
	 * 
	 * @param name
	 *            Name.
	 * @return {@link Object} bound to the name or <code>null</code> if no
	 *         {@link Object} bound by the name.
	 */
	Object getAttribute(String name);

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
	 *            {@link Object}.
	 */
	void setAttribute(String name, Object object);

	/**
	 * Removes the bound {@link Object} by the name.
	 * 
	 * @param name
	 *            Name of bound {@link Object} to remove.
	 */
	void removeAttribute(String name);

}
