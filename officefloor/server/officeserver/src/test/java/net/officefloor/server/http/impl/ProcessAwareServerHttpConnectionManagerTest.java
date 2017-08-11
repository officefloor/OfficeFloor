/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http.impl;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Iterator;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockBufferPool;
import net.officefloor.server.http.mock.MockNonMaterialisedHttpHeaders;
import net.officefloor.server.http.mock.MockProcessAwareContext;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.ByteArrayByteSequence;

/**
 * Tests the {@link ProcessAwareServerHttpConnectionManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareServerHttpConnectionManagerTest extends OfficeFrameTestCase
		implements HttpResponseWriter<byte[]> {

	/**
	 * {@link HttpMethod}.
	 */
	private HttpMethod method = HttpMethod.GET;

	/**
	 * Request URI.
	 */
	private String requestUri = "/";

	/**
	 * {@link HttpVersion}.
	 */
	private final HttpVersion requestVersion = HttpVersion.HTTP_1_1;

	/**
	 * {@link NonMaterialisedHttpHeaders}.
	 */
	private MockNonMaterialisedHttpHeaders requestHeaders = new MockNonMaterialisedHttpHeaders();

	/**
	 * {@link MockBufferPool}.
	 */
	private final MockBufferPool bufferPool = new MockBufferPool();

	/**
	 * Request HTTP entity.
	 */
	private final ByteArrayByteSequence requestEntity = new ByteArrayByteSequence(
			"TEST".getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));

	/**
	 * {@link ProcessAwareServerHttpConnectionManagedObject} to be tested.
	 */
	private final ProcessAwareServerHttpConnectionManagedObject<byte[]> connection = this.createServerHttpConnection();

	/**
	 * Creates a {@link ProcessAwareServerHttpConnectionManagedObject} for
	 * testing.
	 */
	private ProcessAwareServerHttpConnectionManagedObject<byte[]> createServerHttpConnection() {
		ProcessAwareServerHttpConnectionManagedObject<byte[]> connection = new ProcessAwareServerHttpConnectionManagedObject<>(
				true, () -> this.method, () -> this.requestUri, this.requestVersion, this.requestHeaders,
				this.requestEntity, this, this.bufferPool);
		connection.setProcessAwareContext(new MockProcessAwareContext());
		return connection;
	}

	/**
	 * Ensure correct {@link HttpRequest} details.
	 */
	public void testIntialiseConnection() throws IOException {

		// Add a request header
		this.requestHeaders.addHttpHeader("name", "value");

		// Ensure correct connection information
		assertEquals("Incorrect connection method", HttpMethod.GET, this.connection.getHttpMethod());

		// Ensure correct request information
		HttpRequest request = this.connection.getHttpRequest();
		assertEquals("Incorrect request method", HttpMethod.GET, request.getHttpMethod());
		assertEquals("Incorrect request URI", "/", request.getRequestURI());
		assertEquals("Incorrect request version", this.requestVersion, request.getHttpVersion());
		assertEquals("Incorrect number of request headers", 1, request.getHttpHeaders().length());
		assertEquals("Incorrect request header", "value", request.getHttpHeaders().getHeader("name").getValue());

		// Ensure correct request entity content
		StringWriter requestContent = new StringWriter();
		Reader entityReader = new InputStreamReader(request.getEntity(),
				ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
		for (int character = entityReader.read(); character != -1; character = entityReader.read()) {
			requestContent.write(character);
		}
		assertEquals("Incorrect request content", "TEST", requestContent.toString());
	}

	/**
	 * Ensure can send default response.
	 */
	public void testDefaultSend() throws IOException {

		// Send
		this.connection.getHttpResponse().send();

		// Validate the default details
		assertEquals("Incorrect version", this.requestVersion, this.responseVersion);
		assertEquals("Incorect status", HttpStatus.OK, this.status);
		assertFalse("Should be no headers", this.responseHeaders.iterator().hasNext());
		assertEquals("Should be no entity", 0, this.contentLength);
		assertNull("No Content-Type for no entity", this.contentType);
		assertFalse("No entity content", this.content.iterator().hasNext());
	}

	/**
	 * Ensure can send altered information.
	 */
	public void testAlteredSend() throws IOException {

		Charset charset = Charset.forName("UTF-16");

		// Alter the response
		HttpResponse response = this.connection.getHttpResponse();
		response.setHttpStatus(HttpStatus.CREATED);
		response.setHttpVersion(HttpVersion.HTTP_1_0);
		response.getHttpHeaders().addHeader("name", "value");
		response.setContentType("text/html", charset);
		ServerWriter writer = response.getEntityWriter();
		writer.write("TEST RESPONSE");

		// Send response
		response.send();

		// Obtain the expected content
		byte[] expectedContent = "TEST RESPONSE".getBytes(charset);

		// Validate the response
		assertEquals("Incorrect version", HttpVersion.HTTP_1_0, this.responseVersion);
		assertEquals("Incorect status", HttpStatus.CREATED, this.status);
		Iterator<WritableHttpHeader> headers = this.responseHeaders.iterator();
		assertTrue("Should be a headers", headers.hasNext());
		assertEquals("Incorrect header", "name", headers.next().getName());
		assertFalse("Should be just the one header", headers.hasNext());

		// Validate the content details
		assertEquals("Incorrect content length", expectedContent.length, this.contentLength);
		assertEquals("Incorrect content type", "text/html; charset=" + charset.name(), this.contentType.getValue());

		// Validate the content
		MockBufferPool.releaseStreamBuffers(this.content);
		String content = MockBufferPool.getContent(this.content, charset);
		assertEquals("Incorrect content", "TEST RESPONSE", content);
	}

	/**
	 * Ensure send only once. Repeated sends will not do anything.
	 */
	public void testSendOnlyOnce() throws IOException {

		// Set altered status (to check does not change on sending again)
		HttpResponse response = this.connection.getHttpResponse();
		response.setHttpStatus(HttpStatus.BAD_REQUEST);

		// Send
		response.send();
		assertEquals("Should be sent", HttpStatus.BAD_REQUEST, this.status);

		// Change response and send (but now should not send)
		response.setHttpStatus(HttpStatus.OK);
		response.send();

		// Should not re-send (change the response status)
		assertEquals("Should not re-send", HttpStatus.BAD_REQUEST, this.status);
	}

	/*
	 * ===================== HttpResponseWriter ============================
	 */

	private HttpVersion responseVersion = null;

	private HttpStatus status = null;

	private Iterable<WritableHttpHeader> responseHeaders = null;

	private int contentLength;

	private HttpHeaderValue contentType = null;

	private Iterable<StreamBuffer<byte[]>> content = null;

	@Override
	public void writeHttpResponse(HttpVersion version, HttpStatus status, Iterable<WritableHttpHeader> httpHeaders,
			int contentLength, HttpHeaderValue contentType, Iterable<StreamBuffer<byte[]>> content) {
		this.responseVersion = version;
		this.status = status;
		this.responseHeaders = httpHeaders;
		this.contentLength = contentLength;
		this.contentType = contentType;
		this.content = content;
	}

}