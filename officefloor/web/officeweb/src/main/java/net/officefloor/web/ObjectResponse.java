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

package net.officefloor.web;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * <p>
 * Dependency injected interface to send the {@link Object} response.
 * <p>
 * See {@link HttpResponse} for decorating the HTTP response. May also inject
 * {@link ServerHttpConnection} for dynamic decorating.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectResponse<T> {

	/**
	 * Sends the {@link Object}.
	 * 
	 * @param object {@link Object} to send as response.
	 * @throws HttpException If fails to send the {@link Object}.
	 */
	void send(T object) throws HttpException;

}
