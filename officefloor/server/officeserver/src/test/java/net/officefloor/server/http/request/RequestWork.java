/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.server.http.request;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.TestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.request.config.CommunicationConfig;
import net.officefloor.server.http.request.config.HeaderConfig;
import net.officefloor.server.http.request.config.ProcessConfig;
import net.officefloor.server.http.request.config.RequestConfig;
import net.officefloor.server.stream.ServerOutputStream;

/**
 * Services the HTTP requests for the {@link HttpRequestTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class RequestWork {

	/**
	 * Configuration of the communication for the request.
	 */
	private static CommunicationConfig communication;

	/**
	 * Exception thrown in processing response.
	 */
	private static Throwable exception = null;

	/**
	 * Specifies the {@link ProcessConfig} on how to process the request.
	 * 
	 * @param communication
	 *            {@link CommunicationConfig}.
	 */
	public static void setConfiguration(CommunicationConfig communication) {
		synchronized (RequestWork.class) {
			RequestWork.communication = communication;

			// Also clear the exception
			RequestWork.exception = null;
		}
	}

	/**
	 * Obtains the exception thrown in failing to process request.
	 * 
	 * @return Exception thrown in failing to process request.
	 */
	public static Throwable getException() {
		synchronized (RequestWork.class) {
			return RequestWork.exception;
		}
	}

	/**
	 * Processes the HTTP request.
	 * 
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 */
	public void service(ServerHttpConnection connection) throws Throwable {

		// Obtain the configuration of how to process the request
		CommunicationConfig communication;
		synchronized (RequestWork.class) {
			communication = RequestWork.communication;
		}

		// Validate the request
		RequestConfig req = communication.request;
		HttpRequest request = connection.getHttpRequest();
		TestCase.assertEquals("Incorrect method", req.method,
				request.getMethod());
		TestCase.assertEquals("Incorrect request URI", req.path,
				request.getRequestURI());
		TestCase.assertEquals("Incorrect version", req.version,
				request.getVersion());

		// Validate request headers provided
		for (int i = 0; i < req.headers.size(); i++) {
			HeaderConfig headerConfig = req.headers.get(i);
			HttpHeader httpHeader = request.getHeaders().get(i);
			TestCase.assertEquals("Invalid request header name (" + i + ")",
					headerConfig.name, httpHeader.getName());
			TestCase.assertEquals("Invalid request header value ("
					+ headerConfig.name + ", " + i + ")", headerConfig.value,
					httpHeader.getValue());
		}

		// Validate the request body
		Reader reader = new InputStreamReader(request.getEntity());
		StringWriter bodyBuffer = new StringWriter();
		for (int value = reader.read(); value != -1; value = reader.read()) {
			bodyBuffer.write(value);
		}
		String actualBody = (bodyBuffer.getBuffer().length() == 0 ? null
				: bodyBuffer.toString());
		TestCase.assertEquals("Incorrect request body", req.body, actualBody);

		// Throw exception if specified
		ProcessConfig process = communication.process;
		if (process.exception != null) {
			synchronized (RequestWork.class) {
				RequestWork.exception = new RuntimeException(process.exception);
				throw RequestWork.exception;
			}
		}

		// Provide the response
		HttpResponse response = connection.getHttpResponse();
		ServerOutputStream entity = response.getEntity();
		if (process.body != null) {
			Writer writer = new OutputStreamWriter(entity);
			writer.write(process.body);
			writer.flush();
		}

		// Send the response
		connection.getHttpResponse().send();
	}

}