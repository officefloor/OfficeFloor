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
package net.officefloor.server.http.protocol;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.UsAsciiUtil;
import net.officefloor.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.server.http.protocol.HttpCommunicationProtocol;
import net.officefloor.server.impl.AbstractClientServerTestCase;
import net.officefloor.server.impl.AbstractServerSocketManagedObjectSource;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;

/**
 * Tests the {@link HttpCommunicationProtocol}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpCommunicationProtocolTest extends AbstractClientServerTestCase {

	/**
	 * Server name.
	 */
	private final String defaultServerName;

	/**
	 * Initiate the server name.
	 */
	public HttpCommunicationProtocolTest() throws IOException {
		this.defaultServerName = this.getFileContents(this.findFile(HttpCommunicationProtocol.class, "Server.txt"));
	}

	/**
	 * {@link ServerHttpConnection}.
	 */
	private ServerHttpConnection serverHttpConnection;

	/*
	 * ======================= AbstractClientServerTestCase ====================
	 */

	@Override
	protected CommunicationProtocolSource getCommunicationProtocolSource() {
		return new HttpCommunicationProtocol();
	}

	@Override
	protected Properties getCommunicationProtocolProperties() {
		Properties properties = super.getCommunicationProtocolProperties();

		// Mock the date
		properties.setProperty(HttpCommunicationProtocol.PROPERTY_HTTP_SERVER_CLOCK_SOURCE,
				MockHttpServerClock.class.getName());

		// Return the properties
		return properties;
	}

	@Override
	protected void handleInvokeProcess(Object parameter, ManagedObject managedObject, FlowCallback callback) {
		try {
			this.serverHttpConnection = (ServerHttpConnection) managedObject.getObject();
		} catch (Throwable ex) {
			throw fail(ex);
		}
	}

	/**
	 * Ensure the server name has a version (rather than the version tag).
	 * 
	 * Note: this should be replaced within the Maven build.
	 */
	public void testEnsureServerVersionTagReplaced() {
		Pattern pattern = Pattern.compile("^WoOF (\\d)+.(\\d)+.(\\d)+$");
		assertTrue("Should have version tag replaced (" + this.defaultServerName + ")",
				pattern.matcher(this.defaultServerName).matches());
	}

	/**
	 * Ensures can read {@link HttpRequest} with no entity.
	 */
	public void testHttpRequestWithNoEntity() throws Exception {
		this.writeHttpRequest("GET", "/test", null);
		this.runClientSelect();
		this.runServerSelect();
		this.assertHttpRequest("GET", "/test", null);
	}

	/**
	 * Ensures can read {@link HttpRequest} with entity.
	 */
	public void testHttpRequestWithAdditionalHeaderAndEntity() throws Exception {
		this.writeHttpRequest("POST", "/test", "TEST", "header", "value");
		this.runClientSelect();
		this.runServerSelect();
		this.assertHttpRequest("POST", "/test", "TEST", "header", "value");
	}

	/**
	 * Ensure can send {@link HttpResponse}.
	 */
	public void testHttpResponse() throws Exception {

		// Obtain the HTTP response
		HttpResponse response = this.getHttpResponse();

		// Send HTTP response
		ServerOutputStream entity = response.getEntity();
		entity.write(UsAsciiUtil.convertToUsAscii("TEST"));
		entity.close();

		// Validate received response
		this.runServerSelect();
		this.assertHttpResponse(200, "OK", "TEST", "Server", this.defaultServerName, "Date", "[Mock Date]");
	}

	/**
	 * Ensure use default {@link Charset}.
	 */
	public void testHttpResponseDefaultCharset() throws Exception {

		// Obtain the default charset
		final Charset defaultCharset = AbstractServerSocketManagedObjectSource.getCharset(null);

		// Obtain the HTTP response
		HttpResponse response = this.getHttpResponse();
		response.setContentType("text/plain", null);

		// Send HTTP response
		ServerWriter entity = response.getEntityWriter();
		entity.write("TEST");
		entity.close();

		// Validate received response
		this.runServerSelect();
		this.assertHttpResponse(200, "OK", "TEST", "Server", this.defaultServerName, "Date", "[Mock Date]",
				"Content-Type", "text/plain; charset=" + defaultCharset.name());
	}

	/**
	 * Ensure use no {@link Charset} for non-text Content-Type.
	 */
	public void testHttpResponseNoCharset() throws Exception {

		// Obtain the HTTP response
		HttpResponse response = this.getHttpResponse();
		response.setContentType("another/type", null);

		// Send HTTP response
		ServerOutputStream entity = response.getEntity();
		entity.write(UsAsciiUtil.convertToUsAscii("TEST"));
		entity.close();

		// Validate received response
		this.runServerSelect();
		this.assertHttpResponse(200, "OK", "TEST", "Server", this.defaultServerName, "Date", "[Mock Date]",
				"Content-Type", "another/type");
	}

	/**
	 * Ensure able to server short Content-Type.
	 */
	public void testHttpResponseShortContentType() throws Exception {

		// Obtain the HTTP response
		HttpResponse response = this.getHttpResponse();
		response.setContentType("s", null);

		// Send HTTP response
		ServerOutputStream entity = response.getEntity();
		entity.write(UsAsciiUtil.convertToUsAscii("TEST"));
		entity.close();

		// Validate received response
		this.runServerSelect();
		this.assertHttpResponse(200, "OK", "TEST", "Server", this.defaultServerName, "Date", "[Mock Date]",
				"Content-Type", "s");
	}

	/**
	 * Obtains the {@link HttpResponse} for a {@link HttpRequest}.
	 * 
	 * @return {@link HttpResponse} for a {@link HttpRequest}.
	 */
	private HttpResponse getHttpResponse() throws IOException {

		// Send the request to obtain the server HTTP connection
		this.writeHttpRequest("GET", "/test", null);
		this.runClientSelect();
		this.runServerSelect();
		this.assertHttpRequest("GET", "/test", null);

		// As here, have connection so return
		return this.serverHttpConnection.getHttpResponse();
	}

	/**
	 * Writes the {@link HttpRequest}.
	 * 
	 * @param method
	 *            Method.
	 * @param uri
	 *            URI.
	 * @param entity
	 *            Entity content.
	 * @param headerNameValues
	 *            {@link HttpHeader} name value pairs.
	 */
	private void writeHttpRequest(String method, String uri, String entity, String... headerNameValues) {

		// Determine number of bytes to entity
		int contentLength = 0;
		if (entity != null) {
			byte[] entityBytes = UsAsciiUtil.convertToHttp(entity);
			contentLength = entityBytes.length;
		}

		// Create the HTTP request text
		StringBuilder msg = new StringBuilder();
		msg.append(method + " " + uri + " HTTP/1.1\n");
		for (int i = 0; i < headerNameValues.length; i += 2) {
			String name = headerNameValues[i];
			String value = headerNameValues[i + 1];
			msg.append(name + ": " + value + "\n");
		}
		if (contentLength > 0) {
			msg.append("Content-Length: " + contentLength + "\n");
		}
		msg.append("\n");
		if (entity != null) {
			msg.append(entity);
		}

		// Transform for sending
		byte[] msgBytes = UsAsciiUtil.convertToHttp(msg.toString());

		// Write the HTTP request
		this.writeDataFromClientToServer(msgBytes);
	}

	/**
	 * Asserts the current {@link HttpRequest}.
	 * 
	 * @param request
	 *            {@link HttpRequest} to validate.
	 * @param expectedMethod
	 *            Expected method.
	 * @param expectedUri
	 *            Expected URI.
	 * @param expectedEntity
	 *            Expected entity. May be <code>null</code>.
	 * @param expectedHeaderNameValues
	 *            Expected header name value pairs.
	 */
	private void assertHttpRequest(String expectedMethod, String expectedUri, String expectedEntity,
			String... expectedHeaderNameValues) throws IOException {
		assertNotNull("Should have received HTTP request", this.serverHttpConnection);
		assertHttpRequest(this.serverHttpConnection.getHttpRequest(), expectedMethod, expectedUri, expectedEntity,
				expectedHeaderNameValues);
		assertEquals("Incorrect connection HTTP method", expectedMethod, this.serverHttpConnection.getHttpMethod());
	}

	/**
	 * Asserts the {@link HttpResponse} is correct.
	 * 
	 * @param response
	 *            {@link HttpResponse}.
	 * @param expectedStatusCode
	 *            Expected status code.
	 * @param expectedStatusMessage
	 *            Expected status message.
	 * @param expectedEntity
	 *            Expected entity. May be <code>null</code>.
	 * @param expectedHeaderNameValues
	 *            Expected {@link HttpHeader} name/value pairs.
	 */
	private void assertHttpResponse(int expectedStatusCode, String expectedStatusMessage, String expectedEntity,
			String... expectedHeaderNameValues) {

		// Determine number of bytes to entity
		int contentLength = 0;
		if (expectedEntity != null) {
			byte[] entityBytes = UsAsciiUtil.convertToHttp(expectedEntity);
			contentLength = entityBytes.length;
		}

		// Create the HTTP request text
		StringBuilder msg = new StringBuilder();
		msg.append("HTTP/1.1 " + String.valueOf(expectedStatusCode) + " " + expectedStatusMessage + "\n");
		for (int i = 0; i < expectedHeaderNameValues.length; i += 2) {
			String name = expectedHeaderNameValues[i];
			String value = expectedHeaderNameValues[i + 1];
			msg.append(name + ": " + value + "\n");
		}
		if (contentLength > 0) {
			msg.append("Content-Length: " + contentLength + "\n");
		}
		msg.append("\n");
		if (expectedEntity != null) {
			msg.append(expectedEntity);
		}

		// Transform for validating
		byte[] msgBytes = UsAsciiUtil.convertToHttp(msg.toString());

		// Validate the HTTP response
		this.assertClientReceivedData(msgBytes);
	}

	/**
	 * Asserts the {@link HttpRequest}.
	 * 
	 * @param request
	 *            {@link HttpRequest} to validate.
	 * @param expectedMethod
	 *            Expected method.
	 * @param expectedUri
	 *            Expected URI.
	 * @param expectedEntity
	 *            Expected entity. May be <code>null</code>.
	 * @param expectedHeaderNameValues
	 *            Expected header name value pairs.
	 */
	private static void assertHttpRequest(HttpRequest request, String expectedMethod, String expectedUri,
			String expectedEntity, String... expectedHeaderNameValues) throws IOException {

		// Ensure correct details
		assertEquals("Incorrect method", expectedMethod, request.getHttpMethod());
		assertEquals("Incorrect URI", expectedUri, request.getRequestURI());

		// Create the listing of expected headers
		List<HttpHeader> expectedHeaders = new LinkedList<HttpHeader>();
		for (int i = 0; i < expectedHeaderNameValues.length; i += 2) {
			String name = expectedHeaderNameValues[i];
			String value = expectedHeaderNameValues[i + 1];
			expectedHeaders.add(new HttpHeaderImpl(name, value));
		}
		if (expectedEntity != null) {
			// Add the expected content-length
			byte[] entityBytes = UsAsciiUtil.convertToHttp(expectedEntity);
			int contentLength = entityBytes.length;
			expectedHeaders.add(new HttpHeaderImpl("Content-Length", String.valueOf(contentLength)));
		}

		// Validate the headers
		List<HttpHeader> actualHeaders = request.getHeaders();
		assertEquals("Incorrect number of HTTP headers", expectedHeaders.size(), actualHeaders.size());
		for (HttpHeader expectedHeader : expectedHeaders) {

			// Find the corresponding actual header
			HttpHeader actualHeader = null;
			FOUND: for (HttpHeader header : actualHeaders) {
				if (expectedHeader.getName().equals(header.getName())) {
					actualHeader = header;
					break FOUND;
				}
			}
			assertNotNull("HTTP header '" + expectedHeader.getName() + "' not found", actualHeader);
			assertEquals("Incorrect value for HTTP header '" + expectedHeader.getName() + "'",
					expectedHeader.getValue(), actualHeader.getValue());

			// Remove the actual header (as matched)
			actualHeaders.remove(actualHeader);
		}
		assertEquals("Should be no actual headers as all matched", 0, actualHeaders.size());

		// Ensure the entity is as expected
		int available = request.getEntity().available();
		if (expectedEntity == null) {
			// Should be no content
			assertEquals("Should be no entity", -1, available);

		} else {
			// Ensure correct entity content
			assertTrue("Should be entity content", (available >= 0));

			// Obtain the entity content
			byte[] actualEntity = new byte[available];
			request.getEntity().read(actualEntity);
			assertEquals("Incorrect entity content", expectedEntity, UsAsciiUtil.convertToString(actualEntity));
		}
	}

}