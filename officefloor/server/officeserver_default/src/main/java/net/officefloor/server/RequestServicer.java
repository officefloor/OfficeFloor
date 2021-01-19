/*-
 * #%L
 * Default OfficeFloor HTTP Server
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

package net.officefloor.server;

import java.io.IOException;

import net.officefloor.frame.api.manage.ProcessManager;

/**
 * Services requests.
 * 
 * @author Daniel Sagenschneider
 */
public interface RequestServicer<R> {

	/**
	 * Services the request.
	 * 
	 * @param request        Request.
	 * @param responseWriter {@link ResponseWriter}. To enable pipelining of
	 *                       requests, this {@link ResponseWriter} must be invoked
	 *                       to indicate the request has been serviced (even if no
	 *                       data to send).
	 * @return {@link ProcessManager} for servicing the request.
	 * @throws IOException If fails to service the request. This indicates failure
	 *                     in servicing the connection and hence will close the
	 *                     connection.
	 */
	ProcessManager service(R request, ResponseWriter responseWriter) throws IOException;

}
