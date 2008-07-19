/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.http;

import java.io.OutputStreamWriter;

import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;

/**
 * Provides for processing a {@link HttpRequest}.
 * 
 * @author Daniel
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
				+ request.getPath());

		// Obtain response for the request
		HttpResponse response = connection.getHttpResponse();

		// Write the body of the response
		String message = "Hello World";
		new OutputStreamWriter(response.getBody()).append(message).flush();

		// Specify length of the body
		response.addHeader("Content-Length", String.valueOf(message.length()));

		// Send response
		response.send();
	}

}
