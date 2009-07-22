/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.source;

import java.io.OutputStreamWriter;

import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * Provides for processing a {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpWork {

	/**
	 * Processes the {@link HttpRequest}.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 */
	public void service(ServerHttpConnection connection) throws Throwable {

		// Obtain the HTTP request
		HttpRequest request = connection.getHttpRequest();

		// Detail request being serviced
		System.out.println(this.getClass().getSimpleName()
				+ " serving request: " + request.getMethod() + " "
				+ request.getPath() + " " + request.getVersion());

		// Obtain response for the request
		HttpResponse response = connection.getHttpResponse();

		// Write the body of the response
		String message = "Hello World";
		new OutputStreamWriter(response.getBody()).append(message).flush();

		// Send response
		response.send();
	}

}
