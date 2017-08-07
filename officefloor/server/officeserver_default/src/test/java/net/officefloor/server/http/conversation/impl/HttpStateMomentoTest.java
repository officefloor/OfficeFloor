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
package net.officefloor.server.http.conversation.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.clock.HttpServerClock;
import net.officefloor.server.http.conversation.HttpEntity;
import net.officefloor.server.http.conversation.HttpManagedObject;
import net.officefloor.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.server.http.protocol.Connection;
import net.officefloor.server.stream.ServerInputStream;
import net.officefloor.server.stream.impl.ServerInputStreamImpl;

/**
 * Ensure able to use Momento to set state of the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpStateMomentoTest extends OfficeFrameTestCase {

	/**
	 * {@link MockConnection}.
	 */
	private final MockConnection connection = new MockConnection();

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection serverHttpConnection = createConnection("GET", "/request", "HTTP/1.1", null,
			this.connection);

	/**
	 * Ensure can not except an invalid momento.
	 */
	public void testInvalidStateMomento() throws IOException {
		try {
			this.serverHttpConnection.importState(this.createMock(Serializable.class));
			fail("Should not be successful");
		} catch (IllegalArgumentException ex) {
			assertEquals("Incorrect cause", "Invalid momento for ServerHttpConnection", ex.getMessage());
		}
	}

	/**
	 * Ensure able to export and import simple {@link HttpRequest} state.
	 */
	public void testSimpleRequestState() {
		doRequestStateMomentoTest("GET", "/path", "1.1", null);
	}

	/**
	 * Ensure able to export and import complex {@link HttpRequest} state.
	 */
	public void testComplexRequestState() {
		doRequestStateMomentoTest("POST", "/request", "1.0", "Entity Content", "header_one", "value_one", "header_two",
				"value_two");
	}

	/**
	 * Undertakes the State Momento test with the specified parameters for
	 * {@link HttpRequest}.
	 * 
	 * @param method
	 *            HTTP method.
	 * @param requestURI
	 *            HTTP request URI.
	 * @param httpVersion
	 *            HTTP version.
	 * @param entityContent
	 *            Entity content. May be <code>null</code> for no content.
	 * @param headerNameValues
	 *            HTTP header name/value pairs.
	 * @return {@link ServerHttpConnection}.
	 */
	private static void doRequestStateMomentoTest(String method, String requestURI, String httpVersion,
			String entityContent, String... headerNameValues) {
		try {

			// Create the connection (should not need connection)
			ServerHttpConnection connection = createConnection(method, requestURI, httpVersion, entityContent,
					new MockConnection(), headerNameValues);

			// Extract the momento
			Serializable momento = connection.exportState();

			// Serialise the momento
			ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(outputBuffer);
			output.writeObject(momento);
			output.flush();

			// Unserialise the momento
			ByteArrayInputStream inputBuffer = new ByteArrayInputStream(outputBuffer.toByteArray());
			ObjectInputStream input = new ObjectInputStream(inputBuffer);
			Serializable unserialisedMomento = (Serializable) input.readObject();

			// Create new connection to ensure import state
			ServerHttpConnection newConnection = createConnection("OVERRIDE_METHOD", "/override/path",
					"MaintainVersion", "Override content", null, "Header", "Overridden");

			// Override new connection state with momento details
			newConnection.importState(unserialisedMomento);

			// Validate the state is imported
			HttpRequest request = newConnection.getHttpRequest();
			assertEquals("Incorrect method", method, request.getHttpMethod());
			assertEquals("Incorrect path", requestURI, request.getRequestURI());
			assertEquals("Must maintain version of current request to keep HTTP communication/negotiation valid",
					"MaintainVersion", request.getHttpVersion());
			HttpRequestHeaders headers = request.getHttpHeaders();
			assertEquals("Incorrect number of headers", (headerNameValues.length / 2), headers.length());
			for (int i = 0; i < headerNameValues.length; i += 2) {
				String name = headerNameValues[i];
				String value = headerNameValues[i + 1];
				int headerIndex = i / 2;
				HttpHeader header = headers.headerAt(headerIndex);
				assertEquals("Incorrect name for header " + headerIndex, name, header.getName());
				assertEquals("Incorrect value for header " + headerIndex + " (" + name + ")", value, header.getValue());
			}

			// Determine entity correctly
			ServerInputStream entity = request.getEntity();
			if (entityContent == null) {
				// Should have no entity content (end of stream)
				assertEquals("Should have no entity content", -1, entity.available());

			} else {
				// Ensure appropriate entity content is available
				byte[] expectedBytes = entityContent.getBytes();
				assertEquals("Incorrect number of bytes available", expectedBytes.length, entity.available());
				byte[] actualBytes = new byte[expectedBytes.length];
				entity.read(actualBytes);
				assertEquals("Incorrect entity content", entityContent, new String(actualBytes));
				assertEquals("Should now be end of stream", -1, entity.available());
			}

			// Ensure connection still reflects the actual HTTP method
			assertEquals("Incorrect connection HTTP method", "OVERRIDE_METHOD", newConnection.getHttpMethod());

		} catch (Exception ex) {
			fail(ex);
		}
	}

	/**
	 * No {@link HttpResponse} changes.
	 */
	public void testSimpleResponse() throws Exception {
		this.doResponseStateMomentoTest();
	}

	/**
	 * Content is already written for response.
	 */
	public void testNonEmptyEntityResponse() throws Exception {
		this.serverHttpConnection.getHttpResponse().getEntity().write("TEST".getBytes());
		this.doResponseStateMomentoTest();
	}

	/**
	 * Content is already written for response.
	 */
	public void testNonEmptyEntityWriterResponse() throws Exception {
		this.serverHttpConnection.getHttpResponse().getEntityWriter().write("TEST");
		this.doResponseStateMomentoTest();
	}

	/**
	 * {@link HttpResponse} has changed.
	 */
	public void testComplexResponse() throws Exception {
		HttpResponse response = this.serverHttpConnection.getHttpResponse();
		response.setHttpStatus(new HttpStatus(203, "Another status"));
		response.getHttpHeaders().addHeader("HEADER_ONE", "VALUE_ONE");
		response.getHttpHeaders().addHeader("HEADER_TWO", "VALUE_TWO");
		response.setContentType("zip", HttpRequestParserImpl.US_ASCII);
		response.getEntity().write("TEST".getBytes());
		ServerHttpConnection connection = this.doResponseStateMomentoTest();

		// Ensure headers available from response (with correct ordering)
		HttpResponse clonedResponse = connection.getHttpResponse();
//		HttpHeader[] headers = clonedResponse.getHttpHeaders().getHeaders();
//		assertEquals("Incorrect number of headers", 2, headers.length);
//		assertEquals("Incorrect header one", "HEADER_ONE: VALUE_ONE",
//				headers[0].getName() + ": " + headers[0].getValue());
//		assertEquals("Incorrect header two", "HEADER_TWO: VALUE_TWO",
//				headers[1].getName() + ": " + headers[1].getValue());
	}

	/**
	 * Undertakes the State Momento test with the specified parameters for
	 * {@link HttpResponse}.
	 * 
	 * @return Cloned {@link ServerHttpConnection}.
	 */
	private ServerHttpConnection doResponseStateMomentoTest() throws Exception {

		// Extract the momento
		Serializable momento = this.serverHttpConnection.exportState();

		// Serialise the momento
		ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(outputBuffer);
		output.writeObject(momento);
		output.flush();

		// Unserialise the momento
		ByteArrayInputStream inputBuffer = new ByteArrayInputStream(outputBuffer.toByteArray());
		ObjectInputStream input = new ObjectInputStream(inputBuffer);
		Serializable unserialisedMomento = (Serializable) input.readObject();

		// Create the new connection (cloning the state)
		MockConnection newConnection = new MockConnection();
		ServerHttpConnection newServerHttpConnection = createConnection("GET", "/testing/response", "HTTP/1.1", null,
				newConnection);
		newServerHttpConnection.getHttpResponse().setHttpVersion(new HttpVersion("NOT_OVERRIDE_VERSION"));
		newServerHttpConnection.importState(unserialisedMomento);

		// Ensure not override HTTP version
		newServerHttpConnection.getHttpResponse().send();
		String actualContent = new String(newConnection.getWrittenBytes());
		assertTrue("Should not override HTTP version: " + actualContent,
				actualContent.startsWith("NOT_OVERRIDE_VERSION "));

		// Validate same content
		HttpResponse originalResponse = this.serverHttpConnection.getHttpResponse();
		originalResponse.setHttpVersion(new HttpVersion("NOT_OVERRIDE_VERSION"));
		originalResponse.send();
		String expectedContent = new String(this.connection.getWrittenBytes());
		assertEquals("Incorrect cloned response", expectedContent, actualContent);

		// Return the cloned connection
		return newServerHttpConnection;
	}

	/**
	 * Creates a {@link ServerHttpConnection} for testing.
	 * 
	 * @param method
	 *            HTTP method.
	 * @param requestURI
	 *            HTTP request URI.
	 * @param httpVersion
	 *            HTTP version.
	 * @param entityContent
	 *            Entity content. May be <code>null</code> for no content.
	 * @param connection
	 *            {@link Connection}.
	 * @param headerNameValues
	 *            HTTP header name/value pairs.
	 * @return {@link ServerHttpConnection}.
	 */
	private static ServerHttpConnection createConnection(String method, String requestURI, String httpVersion,
			String entityContent, Connection connection, String... headerNameValues) {

		// Create the entity
		ServerInputStreamImpl entity = new ServerInputStreamImpl(new Object());
		if (entityContent == null) {
			// Load no entity data
			entity.inputData(null, 0, 0, false);
		} else {
			// Load the content for the entity
			byte[] contentBytes = entityContent.getBytes();
			entity.inputData(contentBytes, 0, contentBytes.length - 1, false);
		}
		HttpEntity httpEntity = new HttpEntityImpl(entity);

		// Create the conversation
		int sendBufferSize = 1024;
		Charset defaultCharset = Charset.defaultCharset();
		HttpConversationImpl conversation = new HttpConversationImpl(connection, "TEST", sendBufferSize, defaultCharset,
				false, new HttpServerClock() {
					@Override
					public String getDateHeaderValue() {
						return "[Mock Time]";
					}
				});

		// Create the listing of headers
		List<HttpHeader> headers = new LinkedList<HttpHeader>();
		for (int i = 0; i < headerNameValues.length; i += 2) {
			String name = headerNameValues[i];
			String value = headerNameValues[i + 1];
			headers.add(new HttpHeaderImpl(name, value));
		}
		HttpRequestHeaders httpheaders = null;

		// Add the request
		HttpManagedObject mo = conversation.addRequest(method, requestURI, httpVersion, httpheaders, httpEntity);

		// Return the connection
		return mo.getServerHttpConnection();
	}

}