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
package net.officefloor.plugin.socket.server.http.conversation.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.conversation.HttpEntity;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.stream.ServerInputStream;
import net.officefloor.plugin.stream.impl.ServerInputStreamImpl;

/**
 * Ensure able to use Momento to set state of the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpStateMomentoTest extends OfficeFrameTestCase {

	/**
	 * Ensure able to export and import simple state.
	 */
	public void testSimpleState() {
		doRequestStateMomentoTest("GET", "/path", "1.1", null);
	}

	/**
	 * Ensure able to export and import complex state.
	 */
	public void testComplexState() {
		doRequestStateMomentoTest("POST", "/request", "1.0", "Entity Content",
				"header_one", "value_one", "header_two", "value_two");
	}

	/**
	 * Ensure can not except an invalid momento.
	 */
	public void testInvalidStateMomento() {

		// Create the connection
		ServerHttpConnection connection = createConnection("GET", "/path",
				"1.1", null);

		try {
			connection.importState(this.createMock(Serializable.class));
		} catch (IllegalArgumentException ex) {
			assertEquals("Incorrect cause",
					"Invalid momento for ServerHttpConnection", ex.getMessage());
		}
	}

	/**
	 * Undertakes the State Momento test with the specified parameters.
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
	private static void doRequestStateMomentoTest(String method,
			String requestURI, String httpVersion, String entityContent,
			String... headerNameValues) {
		try {

			// Create the connection
			ServerHttpConnection connection = createConnection(method,
					requestURI, httpVersion, entityContent, headerNameValues);

			// Extract the momento
			Serializable momento = connection.exportState();

			// Serialise the momento
			ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
			ObjectOutputStream output = new ObjectOutputStream(outputBuffer);
			output.writeObject(momento);
			output.flush();

			// Unserialise the momento
			ByteArrayInputStream inputBuffer = new ByteArrayInputStream(
					outputBuffer.toByteArray());
			ObjectInputStream input = new ObjectInputStream(inputBuffer);
			Serializable unserialisedMomento = (Serializable) input
					.readObject();

			// Create new connection to ensure import state
			ServerHttpConnection newConnection = createConnection(
					"OVERRIDE_METHOD", "/override/path", "MaintainVersion",
					"Override content", "Header", "Overridden");

			// Override new connection state with momento details
			newConnection.importState(unserialisedMomento);

			// Validate the state is imported
			HttpRequest request = newConnection.getHttpRequest();
			assertEquals("Incorrect method", method, request.getMethod());
			assertEquals("Incorrect path", requestURI, request.getRequestURI());
			assertEquals(
					"Must maintain version of current request to keep HTTP communication/negotiation valid",
					"MaintainVersion", request.getVersion());
			List<HttpHeader> headers = request.getHeaders();
			assertEquals("Incorrect number of headers",
					(headerNameValues.length / 2), headers.size());
			for (int i = 0; i < headerNameValues.length; i += 2) {
				String name = headerNameValues[i];
				String value = headerNameValues[i + 1];
				int headerIndex = i / 2;
				HttpHeader header = headers.get(headerIndex);
				assertEquals("Incorrect name for header " + headerIndex, name,
						header.getName());
				assertEquals("Incorrect value for header " + headerIndex + " ("
						+ name + ")", value, header.getValue());
			}

			// Determine entity correctly
			ServerInputStream entity = request.getEntity();
			if (entityContent == null) {
				// Should have no entity content (end of stream)
				assertEquals("Should have no entity content", -1,
						entity.available());

			} else {
				// Ensure appropriate entity content is available
				byte[] expectedBytes = entityContent.getBytes();
				assertEquals("Incorrect number of bytes available",
						expectedBytes.length, entity.available());
				byte[] actualBytes = new byte[expectedBytes.length];
				entity.read(actualBytes);
				assertEquals("Incorrect entity content", entityContent,
						new String(actualBytes));
				assertEquals("Should now be end of stream", -1,
						entity.available());
			}

			// Ensure connection still reflects the actual HTTP method
			assertEquals("Incorrect connection HTTP method", "OVERRIDE_METHOD",
					newConnection.getHttpMethod());

		} catch (Exception ex) {
			fail(ex);
		}
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
	 * @param headerNameValues
	 *            HTTP header name/value pairs.
	 * @return {@link ServerHttpConnection}.
	 */
	private static ServerHttpConnection createConnection(String method,
			String requestURI, String httpVersion, String entityContent,
			String... headerNameValues) {

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

		// Create the listing of headers
		List<HttpHeader> headers = new LinkedList<HttpHeader>();
		for (int i = 0; i < headerNameValues.length; i += 2) {
			String name = headerNameValues[i];
			String value = headerNameValues[i + 1];
			headers.add(new HttpHeaderImpl(name, value));
		}

		// Create the request
		HttpRequestImpl request = new HttpRequestImpl(method, requestURI,
				httpVersion, headers, httpEntity);

		// Create the connection
		HttpManagedObjectImpl connection = new HttpManagedObjectImpl(null,
				request, null);

		// Return the connection
		return connection;
	}

}