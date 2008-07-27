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
package net.officefloor.plugin.socket.server.http.request;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.TestCase;
import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.request.config.CommunicationConfig;
import net.officefloor.plugin.socket.server.http.request.config.HeaderConfig;
import net.officefloor.plugin.socket.server.http.request.config.ProcessConfig;
import net.officefloor.plugin.socket.server.http.request.config.RequestConfig;

/**
 * Services the HTTP requests for the {@link HttpRequestTest}.
 * 
 * @author Daniel
 */
public class RequestWork {

	/**
	 * Configuration of the communication for the request.
	 */
	private static CommunicationConfig communication;

	/**
	 * Specifies the {@link ProcessConfig} on how to process the request.
	 * 
	 * @param communication
	 *            {@link CommunicationConfig}.
	 */
	public static void setConfiguration(CommunicationConfig communication) {
		synchronized (RequestWork.class) {
			RequestWork.communication = communication;
		}
	}

	/**
	 * Processes the HTTP request.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 */
	public void service(ServerHttpConnection connection) throws Throwable {
		try {
			// Obtain the configuration of how to process the request
			CommunicationConfig communication;
			synchronized (RequestWork.class) {
				communication = RequestWork.communication;
			}

			// Validate the request
			RequestConfig req = communication.request;
			HttpRequest request = connection.getHttpRequest();
			TestCase.assertEquals("Incorrect method", req.method, request
					.getMethod());
			TestCase
					.assertEquals("Incorrect path", req.path, request.getPath());
			TestCase.assertEquals("Incorrect version", req.version, request
					.getVersion());

			// Validate request headers provided
			for (HeaderConfig header : req.headers) {
				TestCase.assertEquals("Invalid request header for name '"
						+ header.name + "'", header.value, request
						.getHeader(header.name));
			}

			// Validate the request body
			Reader reader = new InputStreamReader(request.getBody());
			StringWriter bodyBuffer = new StringWriter();
			for (int value = reader.read(); value != -1; value = reader.read()) {
				bodyBuffer.write(value);
			}
			String actualBody = (bodyBuffer.getBuffer().length() == 0 ? null
					: bodyBuffer.toString());
			TestCase.assertEquals("Incorrect request body", req.body,
					actualBody);

			// Provide the response
			HttpResponse response = connection.getHttpResponse();
			ProcessConfig process = communication.process;
			if (process.body != null) {
				Writer writer = new OutputStreamWriter(response.getBody());
				writer.write(process.body);
				writer.flush();
			}

			// Send the response
			connection.getHttpResponse().send();

		} catch (Throwable ex) {
			// Ensure send response to not have test hang
			connection.getHttpResponse().send();

			// Throw the failure
			throw ex;
		}
	}
}
