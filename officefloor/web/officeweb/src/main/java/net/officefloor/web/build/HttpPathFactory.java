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
