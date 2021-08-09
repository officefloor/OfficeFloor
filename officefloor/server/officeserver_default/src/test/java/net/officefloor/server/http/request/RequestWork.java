/*-
 * #%L
 * Default OfficeFloor HTTP Server
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

package net.officefloor.server.http.request;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.TestCase;
import net.officefloor.plugin.clazz.NonFunctionMethod;
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
	@NonFunctionMethod
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
	@NonFunctionMethod
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
	public static void service(ServerHttpConnection connection) throws Throwable {

		// Obtain the configuration of how to process the request
		CommunicationConfig communication;
		synchronized (RequestWork.class) {
			communication = RequestWork.communication;
		}

		// Validate the request
		RequestConfig req = communication.request;
		HttpRequest request = connection.getRequest();
		TestCase.assertEquals("Incorrect method", req.method, request.getMethod().getName());
		TestCase.assertEquals("Incorrect request URI", req.path, request.getUri());
		TestCase.assertEquals("Incorrect version", req.version, request.getVersion().getName());

		// Validate request headers provided
		for (int i = 0; i < req.headers.size(); i++) {
			HeaderConfig headerConfig = req.headers.get(i);
			HttpHeader httpHeader = request.getHeaders().headerAt(i);
			TestCase.assertEquals("Invalid request header name (" + i + ")", headerConfig.name, httpHeader.getName());
			TestCase.assertEquals("Invalid request header value (" + headerConfig.name + ", " + i + ")",
					headerConfig.value, httpHeader.getValue());
		}

		// Validate the request body
		Reader reader = new InputStreamReader(request.getEntity());
		StringWriter bodyBuffer = new StringWriter();
		for (int value = reader.read(); value != -1; value = reader.read()) {
			bodyBuffer.write(value);
		}
		String actualBody = (bodyBuffer.getBuffer().length() == 0 ? null : bodyBuffer.toString());
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
		HttpResponse response = connection.getResponse();
		ServerOutputStream entity = response.getEntity();
		if (process.body != null) {
			Writer writer = new OutputStreamWriter(entity);
			writer.write(process.body);
			writer.flush();
		}

		// Send the response
		connection.getResponse().send();
	}

}
