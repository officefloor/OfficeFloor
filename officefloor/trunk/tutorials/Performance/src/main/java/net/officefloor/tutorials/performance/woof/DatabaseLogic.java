/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.tutorials.performance.woof;

import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * Logic to service the database request.
 * 
 * @author Daniel Sagenschneider
 */
public class DatabaseLogic {

	/**
	 * Response.
	 */
	private byte[] response = "d".getBytes();

	/**
	 * Services the database request.
	 * 
	 * @param object
	 *            {@link DatabaseConnectionDependency} to identify a database
	 *            dependency.
	 * @param connection
	 *            {@link ServerHttpConnection} to send response.
	 * @throws Exception
	 *             If fails to service.
	 */
	public void service(DatabaseConnectionDependency object,
			ServerHttpConnection connection) throws Exception {

		// Sleep for the millisecond
		Thread.sleep(1);

		// Send the response
		HttpResponse response = connection.getHttpResponse();
		response.getBody().getOutputStream().write(this.response);
		response.send();
	}

}