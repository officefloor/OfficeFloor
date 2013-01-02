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
package net.officefloor.plugin.servlet.socket.server.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.stream.ServerOutputStream;
import net.officefloor.plugin.stream.ServerWriter;

/**
 * Tests the {@link ServletServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServerHttpConnectionTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link HttpServletRequest}.
	 */
	private final HttpServletRequest request = this
			.createMock(HttpServletRequest.class);

	/**
	 * Mock {@link HttpServletResponse}.
	 */
	private final HttpServletResponse response = this
			.createMock(HttpServletResponse.class);

	/**
	 * {@link ServletServerHttpConnection} to test.
	 */
	private ServletServerHttpConnection connection = new ServletServerHttpConnection(
			this.request, this.response);

	/**
	 * Ensure determines correct if secure.
	 */
	public void test_isSecure() {
		this.recordReturn(this.request, this.request.isSecure(), true);
		this.replayMockObjects();
		assertTrue("Should be secure", this.connection.isSecure());
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain local address.
	 */
	public void test_getLocalAddress() {
		final String localAddress = "127.0.0.1";
		final int localPort = 1234;
		this.recordReturn(this.request, this.request.getLocalAddr(),
				localAddress);
		this.recordReturn(this.request, this.request.getLocalPort(), localPort);
		this.replayMockObjects();
		InetSocketAddress address = this.connection.getLocalAddress();
		this.verifyMockObjects();
		assertEquals("Incorrect local name", localAddress, address.getAddress()
				.getHostAddress());
		assertEquals("Incorrect local port", localPort, address.getPort());
	}

	/**
	 * Ensure able to obtain remote address.
	 */
	public void test_getRemoteAddress() {
		final String remoteAddress = "192.168.0.1";
		final int remotePort = 4321;
		this.recordReturn(this.request, this.request.getRemoteAddr(),
				remoteAddress);
		this.recordReturn(this.request, this.request.getRemotePort(),
				remotePort);
		this.replayMockObjects();
		InetSocketAddress address = this.connection.getRemoteAddress();
		this.verifyMockObjects();
		assertEquals("Incorrect local name", remoteAddress, address
				.getAddress().getHostAddress());
		assertEquals("Incorrect local port", remotePort, address.getPort());
	}

	/**
	 * Ensure able to obtain client method.
	 */
	public void test_getMethod() {
		final String METHOD = "GET";
		this.recordReturn(this.request, this.request.getMethod(), METHOD);
		this.replayMockObjects();
		assertEquals("Incorrect method", METHOD,
				this.connection.getHttpMethod());
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to export/import state.
	 */
	@SuppressWarnings("unchecked")
	public void test_exportImportState() throws Exception {

		final String METHOD = "POST";
		final String CONTEXT_PATH = "/path";
		final String REQUEST_URI = "/test";
		final String QUERY_STRING = "name=value";
		Enumeration<String> headerNames = this.createMock(Enumeration.class);
		final String HEADER_NAME = "HEADER_NAME";
		Enumeration<String> headerValues = this.createMock(Enumeration.class);
		final String HEADER_VALUE = "HEADER_VALUE";
		InputStream requestEntity = new ServletInputStream() {
			@Override
			public int read() throws IOException {
				return 1;
			}
		};

		// Record initially adding response header and writing content
		this.response.addHeader("NAME", "VALUE");
		this.recordReturn(this.response, this.response.getOutputStream(),
				new ServletOutputStream() {
					@Override
					public void write(int b) throws IOException {
						fail("Should not write out content");
					}
				});

		// Record export
		this.recordReturn(this.request, this.request.getMethod(), METHOD);
		this.recordReturn(this.request, this.request.getRequestURI(),
				CONTEXT_PATH + REQUEST_URI);
		this.recordReturn(this.request, this.request.getQueryString(),
				QUERY_STRING);
		this.recordReturn(this.request, this.request.getHeaderNames(),
				headerNames);
		this.recordReturn(headerNames, headerNames.hasMoreElements(), true);
		this.recordReturn(headerNames, headerNames.nextElement(), HEADER_NAME);
		this.recordReturn(this.request, this.request.getHeaders(HEADER_NAME),
				headerValues);
		this.recordReturn(headerValues, headerValues.hasMoreElements(), true);
		this.recordReturn(headerValues, headerValues.nextElement(),
				HEADER_VALUE);
		this.recordReturn(headerValues, headerValues.hasMoreElements(), false);
		this.recordReturn(headerNames, headerNames.hasMoreElements(), false);
		this.recordReturn(this.request, this.request.getContentLength(), 1);
		this.recordReturn(this.request, this.request.getInputStream(),
				requestEntity);

		// Record import
		this.response.setHeader("NAME", "VALUE");

		// Record continue to use Servlet values
		this.recordReturn(this.request, this.request.getProtocol(), "HTTP/1.1");
		this.recordReturn(this.request, this.request.getMethod(), "GET");

		// Record flushing response
		final ByteArrayOutputStream responseEntity = new ByteArrayOutputStream();
		this.recordReturn(this.response, this.response.getOutputStream(),
				new ServletOutputStream() {
					@Override
					public void write(int b) throws IOException {
						responseEntity.write(b);
					}
				});

		// Test
		this.replayMockObjects();

		// Provide response details that should exported
		this.connection.getHttpResponse().addHeader("NAME", "VALUE");
		this.connection.getHttpResponse().getEntity().write("TEST".getBytes());

		// Export the state
		Serializable momento = this.connection.exportState();

		// Serialise the momento
		ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
		ObjectOutputStream output = new ObjectOutputStream(outputBuffer);
		output.writeObject(momento);
		output.flush();

		// Unserialise the momento
		ByteArrayInputStream inputBuffer = new ByteArrayInputStream(
				outputBuffer.toByteArray());
		ObjectInputStream input = new ObjectInputStream(inputBuffer);
		Serializable unserialisedMomento = (Serializable) input.readObject();

		// Create a new connection
		ServerHttpConnection clone = new ServletServerHttpConnection(
				this.request, this.response);
		clone.importState(unserialisedMomento);

		// Ensure use request state from momento
		HttpRequest request = clone.getHttpRequest();
		assertEquals("Incorrect method", METHOD, request.getMethod());
		assertEquals("Incorrect request URI", CONTEXT_PATH + REQUEST_URI + "?"
				+ QUERY_STRING, request.getRequestURI());
		assertEquals("Incorrect version", "HTTP/1.1", request.getVersion());
		List<HttpHeader> headers = request.getHeaders();
		assertEquals("Incorrect number of headers", 1, headers.size());
		HttpHeader header = headers.get(0);
		assertEquals("Incorrect header name", HEADER_NAME, header.getName());
		assertEquals("Incorrect header value", HEADER_VALUE, header.getValue());
		assertEquals("Incorrect request entity byte", 1, request.getEntity()
				.read());
		assertEquals("Request entity to have only one byte", -1, request
				.getEntity().available());

		// Ensure use servlet request method
		assertEquals("Incorrect client HTTP method", "GET",
				this.connection.getHttpMethod());

		// Ensure response state
		HttpResponse response = clone.getHttpResponse();
		assertEquals("Incorrect response header value", "VALUE", response
				.getHeader("NAME").getValue());
		response.getEntity().write("_ANOTHER".getBytes());
		response.getEntity().flush();
		assertEquals("Incorrect response entity", "TEST_ANOTHER", new String(
				responseEntity.toByteArray()));

		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain request method.
	 */
	public void test_getHttpRequest_getMethod() {
		final String METHOD = "GET";
		this.recordReturn(this.request, this.request.getMethod(), METHOD);
		this.replayMockObjects();
		HttpRequest request = this.connection.getHttpRequest();
		assertEquals("Incorrect method", METHOD, request.getMethod());
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain request URI.
	 */
	public void test_getHttpRequest_getRequestURI() {
		final String CONTEXT_PATH = "/path";
		final String REQUEST_URI = "/test";
		final String QUERY_STRING = "name=value";
		this.recordReturn(this.request, this.request.getRequestURI(),
				CONTEXT_PATH + REQUEST_URI);
		this.recordReturn(this.request, this.request.getQueryString(),
				QUERY_STRING);
		this.replayMockObjects();
		HttpRequest request = this.connection.getHttpRequest();
		assertEquals("Incorrect request URI", CONTEXT_PATH + REQUEST_URI + "?"
				+ QUERY_STRING, request.getRequestURI());
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain request version.
	 */
	public void test_getHttpRequest_getVersion() {
		final String VERSION = "HTTP/1.1";
		this.recordReturn(this.request, this.request.getProtocol(), VERSION);
		this.replayMockObjects();
		HttpRequest request = this.connection.getHttpRequest();
		assertEquals("Incorrect request version", VERSION, request.getVersion());
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain request version.
	 */
	@SuppressWarnings("unchecked")
	public void test_getHttpRequest_getHeaders() {

		final String NAME = "name";
		final String VALUE_ONE = "value1";
		final String VALUE_TWO = "value2";
		final String ANOTHER_NAME = "another";
		final String ANOTHER_VALUE = "valueAnother";

		// Record obtaining headers
		Enumeration<String> headerNames = this.createMock(Enumeration.class);
		this.recordReturn(this.request, this.request.getHeaderNames(),
				headerNames);
		this.recordReturn(headerNames, headerNames.hasMoreElements(), true);
		this.recordReturn(headerNames, headerNames.nextElement(), NAME);
		Enumeration<String> headerValues = this.createMock(Enumeration.class);
		this.recordReturn(this.request, this.request.getHeaders(NAME),
				headerValues);
		this.recordReturn(headerValues, headerValues.hasMoreElements(), true);
		this.recordReturn(headerValues, headerValues.nextElement(), VALUE_ONE);
		this.recordReturn(headerValues, headerValues.hasMoreElements(), true);
		this.recordReturn(headerValues, headerValues.nextElement(), VALUE_TWO);
		this.recordReturn(headerValues, headerValues.hasMoreElements(), false);
		this.recordReturn(headerNames, headerNames.hasMoreElements(), true);
		this.recordReturn(headerNames, headerNames.nextElement(), ANOTHER_NAME);
		Enumeration<String> anotherValues = this.createMock(Enumeration.class);
		this.recordReturn(this.request, this.request.getHeaders(ANOTHER_NAME),
				anotherValues);
		this.recordReturn(anotherValues, anotherValues.hasMoreElements(), true);
		this.recordReturn(anotherValues, anotherValues.nextElement(),
				ANOTHER_VALUE);
		this.recordReturn(anotherValues, anotherValues.hasMoreElements(), false);
		this.recordReturn(headerNames, headerNames.hasMoreElements(), false);

		// Test
		this.replayMockObjects();
		HttpRequest httpRequest = this.connection.getHttpRequest();
		List<HttpHeader> headers = httpRequest.getHeaders();
		this.verifyMockObjects();

		// Ensure correct name/values for headers
		String[] nameValues = new String[] { NAME, VALUE_ONE, NAME, VALUE_TWO,
				ANOTHER_NAME, ANOTHER_VALUE };
		for (int i = 0; i < nameValues.length; i += 2) {
			String name = nameValues[i];
			String value = nameValues[i + 1];
			int index = i / 2;
			HttpHeader header = headers.get(index);
			assertEquals("Incorrect name of header " + index, name,
					header.getName());
			assertEquals("Incorrect value of header " + index, value,
					header.getValue());
		}
	}

	/**
	 * Ensure able to obtain the {@link InputStream}.
	 */
	public void test_getHttpRequest_getInputStream() throws Exception {

		// Record indicating size
		this.recordReturn(this.request, this.request.getContentLength(), 1);

		// Record loading the data
		InputStream expected = new ServletInputStream() {
			@Override
			public int read() throws IOException {
				return 1;
			}
		};
		this.recordReturn(this.request, this.request.getInputStream(), expected);

		// Test
		this.replayMockObjects();
		HttpRequest httpRequest = this.connection.getHttpRequest();
		InputStream actual = httpRequest.getEntity();
		this.verifyMockObjects();

		// Ensure correct input
		assertEquals("Incorrect input stream", 1, actual.read());
		assertEquals("Should be EOF", -1, actual.read());
	}

	/**
	 * Ensure can add {@link HttpHeader}.
	 */
	public void test_getHttpResponse_addHeader() throws Exception {
		this.response.addHeader("name", "value");

		// Test
		this.replayMockObjects();
		HttpResponse httpResponse = this.connection.getHttpResponse();
		HttpHeader header = httpResponse.addHeader("name", "value");
		this.verifyMockObjects();

		// Ensure correct header
		assertEquals("Incorrect header name", "name", header.getName());
		assertEquals("Incorrect header value", "value", header.getValue());
	}

	/**
	 * Ensure can obtain {@link HttpHeader}.
	 */
	public void test_getHttpResponse_getHeader() throws Exception {
		this.response.addHeader("name", "value");

		// Test
		this.replayMockObjects();
		HttpResponse httpResponse = this.connection.getHttpResponse();
		HttpHeader addedHeader = httpResponse.addHeader("name", "value");
		HttpHeader header = httpResponse.getHeader("name");
		this.verifyMockObjects();

		// Ensure correct header
		assertEquals("Incorrect header name", "name", header.getName());
		assertEquals("Incorrect header value", "value", header.getValue());
		assertSame("Incorrect header", addedHeader, header);
	}

	/**
	 * Ensure can get headers.
	 */
	public void test_getHttpResponse_getHeaders() throws Exception {
		this.response.addHeader("name", "value");

		// Test
		this.replayMockObjects();
		HttpResponse httpResponse = this.connection.getHttpResponse();
		HttpHeader addedHeader = httpResponse.addHeader("name", "value");
		HttpHeader[] headers = httpResponse.getHeaders();
		this.verifyMockObjects();

		// Ensure correct headers
		assertEquals("Incorrect number of headers", 1, headers.length);
		HttpHeader header = headers[0];
		assertSame("Incorrect header", addedHeader, header);
	}

	/**
	 * Ensure can remove {@link HttpHeader}.
	 */
	public void test_getHttpResponse_removeHeader() throws Exception {
		this.response.addHeader("name", "value");
		this.response.setHeader("name", null);

		// Test
		this.replayMockObjects();
		HttpResponse httpResponse = this.connection.getHttpResponse();
		HttpHeader header = httpResponse.addHeader("name", "value");
		httpResponse.removeHeader(header);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can remove {@link HttpHeader} instances.
	 */
	public void test_getHttpResponse_removeHeaders() throws Exception {
		this.response.setHeader("name", null);

		// Test
		this.replayMockObjects();
		HttpResponse httpResponse = this.connection.getHttpResponse();
		httpResponse.removeHeaders("name");
		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify the status.
	 */
	public void test_getHttpResponse_setStatus() {
		this.response.setStatus(203);
		this.response.setStatus(500); // not sending message as may not be error

		// Test
		this.replayMockObjects();
		HttpResponse httpResponse = this.connection.getHttpResponse();
		httpResponse.setStatus(203);
		httpResponse.setStatus(500, "test message");
		this.verifyMockObjects();
	}

	/**
	 * Ensure can specify the version.
	 */
	public void test_getHttpResponse_setVersion() {
		// Do nothing as allow Servlet container to manage

		// Test
		this.replayMockObjects();
		HttpResponse httpResponse = this.connection.getHttpResponse();
		httpResponse.setVersion("HTTP/1.1");
		this.verifyMockObjects();
	}

	/**
	 * Ensure can send.
	 */
	public void test_getHttpResponse_send() throws IOException {
		this.response.flushBuffer(); // Servlet container will manage sending

		// Test
		this.replayMockObjects();
		HttpResponse httpResponse = this.connection.getHttpResponse();
		httpResponse.send();
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain the {@link OutputStream} and write data.
	 */
	public void test_getHttpResponse_getOutputStream_writeData()
			throws Exception {

		// Capture the written data
		final byte[] data = new byte[1];
		data[0] = 0;
		OutputStream expected = new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				data[0] = (byte) b;
			}
		};
		this.recordReturn(this.response, this.response.getOutputStream(),
				expected);

		// Test
		this.replayMockObjects();
		HttpResponse httpResponse = this.connection.getHttpResponse();
		OutputStream actual = httpResponse.getEntity();
		actual.write(1);
		actual.flush();
		this.verifyMockObjects();

		// Ensure correct output
		assertEquals("Incorrect output stream", 1, data[0]);
	}

	/**
	 * Ensure able to obtain the {@link OutputStream} write {@link ByteBuffer}.
	 */
	public void test_getHttpResponse_getOutputStream_writeBuffer()
			throws Exception {

		// Capture the written data
		final byte[] data = new byte[1];
		data[0] = 0;
		OutputStream expected = new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				data[0] = (byte) b;
			}
		};
		this.recordReturn(this.response, this.response.getOutputStream(),
				expected);

		// Test
		this.replayMockObjects();
		HttpResponse httpResponse = this.connection.getHttpResponse();
		ServerOutputStream actual = httpResponse.getEntity();
		actual.write(ByteBuffer.wrap(new byte[] { 1 }));
		actual.flush();
		this.verifyMockObjects();

		// Ensure correct output
		assertEquals("Incorrect output stream", 1, data[0]);
	}

	/**
	 * Ensure able to obtain the {@link Writer}.
	 */
	public void test_getHttpResponse_getWriter() throws Exception {

		// Record obtaining the character encoding
		this.recordReturn(this.response, this.response.getCharacterEncoding(),
				Charset.defaultCharset().name());

		// Capture the written data
		final byte[] data = new byte[1];
		data[0] = 0;
		OutputStream expected = new ServletOutputStream() {
			@Override
			public void write(int b) throws IOException {
				data[0] = (byte) b;
			}
		};
		this.recordReturn(this.response, this.response.getOutputStream(),
				expected);

		// Test
		this.replayMockObjects();
		HttpResponse httpResponse = this.connection.getHttpResponse();
		ServerWriter actual = httpResponse.getEntityWriter();
		actual.write('a');
		actual.flush();
		this.verifyMockObjects();

		// Ensure correct output
		assertEquals("Incorrect writer", 'a', ((char) data[0]));
	}

}