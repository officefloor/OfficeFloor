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
package net.officefloor.server.http.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpEscalationHandler;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.http.impl.HttpResponseWriter;
import net.officefloor.server.http.impl.MaterialisingHttpRequest;
import net.officefloor.server.http.impl.MaterialisingHttpRequestHeaders;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareHttpResponse;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteArrayByteSequence;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 * Mock {@link HttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpServer implements HttpServerLocation, HttpServerImplementation {

	/**
	 * {@link ThreadLocalStreamBufferPool} to use for stress testing.
	 */
	private static ThreadLocalStreamBufferPool STRESS_STREAM_BUFFER_POOL = new ThreadLocalStreamBufferPool(
			() -> ByteBuffer.allocate(1024), Integer.MAX_VALUE, Integer.MAX_VALUE);

	/**
	 * Configures the {@link MockHttpServer} to be serviced by the
	 * {@link DeployedOfficeInput}.
	 * 
	 * @param input
	 *            {@link DeployedOfficeInput}.
	 * @return {@link MockHttpServer} to send {@link HttpRequest} instances.
	 */
	public static MockHttpServer configureMockHttpServer(DeployedOfficeInput input) {
		MockHttpServer httpServer = new MockHttpServer();
		new HttpServer(httpServer, httpServer, true, null, input, null, null);
		return httpServer;
	}

	/**
	 * Creates the {@link MockHttpRequestBuilder}.
	 * 
	 * @return {@link MockHttpRequestBuilder}.
	 */
	public static MockHttpRequestBuilder mockRequest() {
		return new MockHttpRequestBuilderImpl();
	}

	/**
	 * Convenience method to create a {@link MockHttpRequestBuilder}.
	 * 
	 * @param requestUri
	 *            Request URI.
	 * @return {@link MockHttpRequestBuilder}.
	 */
	public static MockHttpRequestBuilder mockRequest(String requestUri) {
		MockHttpRequestBuilderImpl request = new MockHttpRequestBuilderImpl();
		request.uri(requestUri);
		return request;
	}

	/**
	 * Creates the {@link MockHttpRequestBuilder}.
	 * 
	 * @return {@link MockHttpRequestBuilder}.
	 */
	public static MockHttpResponseBuilder mockResponse() {
		return new MockHttpResponseBuilderImpl();
	}

	/**
	 * Convenience method to create the {@link MockServerHttpConnection} for GET
	 * / with no headers nor entity.
	 * 
	 * @return {@link MockServerHttpConnection}.
	 */
	public static MockServerHttpConnection mockConnection() {
		return new MockServerHttpConnectionImpl(mockRequest());
	}

	/**
	 * Creates the {@link MockServerHttpConnection}.
	 * 
	 * @param request
	 *            {@link MockHttpRequestBuilder} for the {@link HttpRequest} of
	 *            the {@link MockServerHttpConnection}.
	 * @return {@link MockServerHttpConnection}.
	 */
	public static MockServerHttpConnection mockConnection(MockHttpRequestBuilder request) {
		return new MockServerHttpConnectionImpl(request);
	}

	/**
	 * Obtains the HTTP entity content from the {@link HttpRequest}.
	 * 
	 * @param request
	 *            {@link HttpRequest}.
	 * @param charset
	 *            {@link Charset}. May be <code>null</code> to use default
	 *            {@link Charset}.
	 * @return HTTP entity content.
	 */
	public static String getContent(HttpRequest request, Charset charset) {

		// Ensure have charset
		if (charset == null) {
			charset = ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET;
		}

		// Obtain the content
		StringWriter content = new StringWriter();
		try {
			InputStreamReader reader = new InputStreamReader(request.getEntity().createBrowseInputStream(), charset);
			for (int character = reader.read(); character != -1; character = reader.read()) {
				content.write(character);
			}
		} catch (IOException ex) {
			throw OfficeFrameTestCase.fail(ex);
		}

		// Return the content
		return content.toString();
	}

	/**
	 * Creates the {@link ProcessAwareServerHttpConnectionManagedObject}.
	 * 
	 * @param request
	 *            {@link MockHttpRequestBuilder}.
	 * @param serverLocation
	 *            {@link HttpServerLocation}.
	 * @param callback
	 *            {@link MockHttpRequestCallback}.
	 * @return {@link ProcessAwareServerHttpConnectionManagedObject}.
	 */
	private static ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> createServerHttpConnection(
			MockHttpRequestBuilder request, HttpServerLocation serverLocation, MockHttpRequestCallback callback) {

		// Ensure have implementation
		if (!(request instanceof MockHttpRequestBuilderImpl)) {
			throw new IllegalArgumentException("Must create request with createMockHttpRequest()");
		}
		MockHttpRequestBuilderImpl impl = (MockHttpRequestBuilderImpl) request;

		// Create the inputs
		boolean isSecure = impl.isSecure;
		Supplier<HttpMethod> methodSupplier = () -> impl.method;
		Supplier<String> requestUriSupplier = () -> impl.requestUri;
		HttpVersion requestVersion = impl.version;
		NonMaterialisedHttpHeaders requestHeaders = impl;
		ByteSequence requestEntity = new ByteArrayByteSequence(impl.entity.toByteArray());

		// Provide the buffer pool (based on efficiency)
		StreamBufferPool<ByteBuffer> bufferPool;
		MockStreamBufferPool checkBufferPool;
		if (impl.isStress) {
			bufferPool = STRESS_STREAM_BUFFER_POOL;
			checkBufferPool = null; // do not run checks
		} else {
			// Provide mock buffer pool (for checks)
			checkBufferPool = new MockStreamBufferPool(() -> ByteBuffer.allocate(1024));
			bufferPool = checkBufferPool;
		}

		// Handle response
		HttpResponseWriter<ByteBuffer> responseWriter = new MockHttpResponseWriter(callback, checkBufferPool);

		// Create the server HTTP connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<>(
				serverLocation, isSecure, methodSupplier, requestUriSupplier, requestVersion, requestHeaders,
				requestEntity, true, responseWriter, bufferPool);

		// Return the connection
		return connection;
	}

	/**
	 * {@link ExternalServiceInput}.
	 */
	private ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>> serviceInput;

	/**
	 * Instantiated via static methods.
	 */
	private MockHttpServer() {
	}

	/**
	 * Sends the {@link MockHttpRequestBuilder}.
	 * 
	 * @param request
	 *            {@link MockHttpRequestBuilder}.
	 * @param callback
	 *            {@link MockHttpRequestCallback} to receive the
	 *            {@link MockHttpResponse}.
	 */
	public void send(MockHttpRequestBuilder request, MockHttpRequestCallback callback) {

		// Create the server HTTP connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = createServerHttpConnection(request, this,
				callback);

		// Service the request
		this.serviceInput.service(connection, connection.getServiceFlowCallback());
	}

	/**
	 * {@link HttpResponseWriter} to write the {@link MockHttpResponse}.
	 */
	private static class MockHttpResponseWriter implements HttpResponseWriter<ByteBuffer> {

		/**
		 * {@link MockHttpRequestCallback}.
		 */
		private final MockHttpRequestCallback callback;

		/**
		 * {@link MockStreamBufferPool}.
		 */
		private final MockStreamBufferPool bufferPool;

		/**
		 * Instantiate.
		 * 
		 * @param callback
		 *            {@link MockHttpRequestCallback}.
		 * @param bufferPool
		 *            {@link MockStreamBufferPool} to check all
		 *            {@link StreamBuffer} instances are released on writing
		 *            {@link HttpResponse}. May be <code>null</code>.
		 */
		private MockHttpResponseWriter(MockHttpRequestCallback callback, MockStreamBufferPool bufferPool) {
			this.callback = callback;
			this.bufferPool = bufferPool;
		}

		/*
		 * ================== HttpResponseWriter ==================
		 */

		@Override
		public void writeHttpResponse(HttpVersion version, HttpStatus status, WritableHttpHeader headHttpHeader,
				long contentLength, HttpHeaderValue contentType, StreamBuffer<ByteBuffer> contentHeadStreamBuffer) {
			try {
				// Obtain the listing of response HTTP headers
				List<WritableHttpHeader> headers = new ArrayList<>();
				if (contentType != null) {
					headers.add(new WritableHttpHeader("content-type", contentType));
				}
				if (contentLength > 0) {
					headers.add(new WritableHttpHeader("content-length", String.valueOf(contentLength)));
				}
				while (headHttpHeader != null) {
					headers.add(headHttpHeader);
					headHttpHeader = headHttpHeader.next;
				}

				// Copy out the response entity
				InputStream responseEntityInputStream = MockStreamBufferPool.createInputStream(contentHeadStreamBuffer);
				byte[] responseEntity = new byte[(int) contentLength];
				for (int i = 0; i < contentLength; i++) {
					responseEntity[i] = (byte) responseEntityInputStream.read();
				}

				// Release all the buffers (as now considered written)
				StreamBuffer<ByteBuffer> buffer = contentHeadStreamBuffer;
				while (buffer != null) {
					StreamBuffer<ByteBuffer> release = buffer;
					buffer = buffer.next;
					release.release();
				}

				// Ensure all buffers released (if not stress test)
				if (this.bufferPool != null) {
					this.bufferPool.assertAllBuffersReturned();
				}

				// Create the response
				MockHttpResponseImpl response = new MockHttpResponseImpl(version, status, headers,
						new ByteArrayInputStream(responseEntity));

				// Response received
				this.callback.response(response);

			} catch (Throwable ex) {
				// Provide failed HTTP response
				this.callback.response(new MockHttpResponseImpl(ex));
			}
		}
	}

	/**
	 * Sends the {@link MockHttpRequestBuilder} and blocks waiting for the
	 * {@link MockHttpResponse}.
	 * 
	 * @param request
	 *            {@link MockHttpRequestBuilder}.
	 * @eturn {@link MockHttpResponse}.
	 */
	public MockHttpResponse send(MockHttpRequestBuilder request) {

		// Create the synchronous callback
		SynchronousMockHttpRequestCallback callback = new SynchronousMockHttpRequestCallback();

		// Undertake the request
		this.send(request, callback);

		// Block waiting for response
		return callback.waitForResponse(1000);
	}

	/*
	 * ===================== HttpServerLocation ================================
	 */

	@Override
	public String getDomain() {
		return "mock.officefloor.net";
	}

	@Override
	public int getHttpPort() {
		return 80;
	}

	@Override
	public int getHttpsPort() {
		return 443;
	}

	@Override
	public String getClusterHostName() {
		return "testnode.officefloor.net";
	}

	@Override
	public int getClusterHttpPort() {
		return 7878;
	}

	@Override
	public int getClusterHttpsPort() {
		return 7979;
	}

	@Override
	public String createClientUrl(boolean isSecure, String path) {
		return (isSecure ? "https" : "http") + "://" + this.getDomain();
	}

	/*
	 * ================= HttpServerImplementation ==================
	 */

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) {
		@SuppressWarnings("unchecked")
		Class<ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>> managedObjectType = (Class<ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>>) (Object) ProcessAwareServerHttpConnectionManagedObject.class;
		this.serviceInput = context.getExternalServiceInput(managedObjectType,
				ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());
	}

	/**
	 * {@link MockHttpRequestBuilder} implementation.
	 */
	private static class MockHttpRequestBuilderImpl implements MockHttpRequestBuilder, NonMaterialisedHttpHeaders {

		/**
		 * Indicates if secure.
		 */
		private boolean isSecure = false;

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
		private HttpVersion version = HttpVersion.HTTP_1_1;

		/**
		 * {@link HttpHeader} instances.
		 */
		private final List<MockNonMaterialisedHttpHeader> headers = new LinkedList<>();

		/**
		 * HTTP entity.
		 */
		private final ByteArrayOutputStream entity = new ByteArrayOutputStream();

		/**
		 * Indicates if running stress test for request.
		 */
		private boolean isStress = false;

		/*
		 * ================== MockHttpRequestBuilder ========================
		 */

		@Override
		public MockHttpRequestBuilder secure(boolean isSecure) {
			this.isSecure = isSecure;
			return this;
		}

		@Override
		public MockHttpRequestBuilder method(HttpMethod method) {
			this.method = method;
			return this;
		}

		@Override
		public MockHttpRequestBuilder uri(String requestUri) {
			this.requestUri = requestUri;
			return this;
		}

		@Override
		public MockHttpRequestBuilder version(HttpVersion version) {
			this.version = version;
			return this;
		}

		@Override
		public MockHttpRequestBuilder header(String name, String value) {
			this.headers.add(new MockNonMaterialisedHttpHeader(name, value));
			return this;
		}

		@Override
		public MockHttpRequestBuilder entity(String entity) {

			// Do nothing if no entity content
			if (entity == null) {
				return this;
			}

			// Load the entity
			try {
				this.entity.write(entity.getBytes(ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET));
			} catch (IOException ex) {
				throw OfficeFrameTestCase.fail(ex);
			}
			return this;
		}

		@Override
		public OutputStream getHttpEntity() {
			return this.entity;
		}

		@Override
		public MockHttpRequestBuilder setEfficientForStressTests(boolean isStress) {
			this.isStress = isStress;
			return this;
		}

		@Override
		public HttpRequest build() {
			// Return the request (capturing current content)
			final HttpMethod method = this.method;
			final String requestUri = this.requestUri;
			final MaterialisingHttpRequestHeaders headers = new MaterialisingHttpRequestHeaders(this);
			final ByteArrayByteSequence entity = new ByteArrayByteSequence(this.entity.toByteArray());
			return new MaterialisingHttpRequest(() -> method, () -> requestUri, this.version, headers, entity);
		}

		/*
		 * ================== NonMaterialisedHttpHeaders =====================
		 */

		@Override
		public Iterator<NonMaterialisedHttpHeader> iterator() {
			final Iterator<MockNonMaterialisedHttpHeader> iterator = this.headers.iterator();
			return new Iterator<NonMaterialisedHttpHeader>() {
				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public NonMaterialisedHttpHeader next() {
					return iterator.next();
				}
			};
		}

		@Override
		public int length() {
			return this.headers.size();
		}
	}

	/**
	 * {@link NonMaterialisedHttpHeader}.
	 */
	private static class MockNonMaterialisedHttpHeader implements NonMaterialisedHttpHeader, HttpHeader {

		/**
		 * {@link HttpHeader} name.
		 */
		private final String name;

		/**
		 * {@link HttpHeader} value.
		 */
		private final String value;

		/**
		 * Instantiate.
		 * 
		 * @param name
		 *            {@link HttpHeader} name.
		 * @param value
		 *            {@link HttpHeader} value.
		 */
		public MockNonMaterialisedHttpHeader(String name, String value) {
			this.name = name;
			this.value = value;
		}

		/*
		 * ================= NonMaterialisedHttpHeader ========================
		 */

		@Override
		public HttpHeader materialiseHttpHeader() {
			return this;
		}

		/*
		 * ======================== HttpHeader ================================
		 */

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getValue() {
			return this.value;
		}
	}

	/**
	 * {@link MockHttpResponseBuilder} implementation.
	 */
	private static class MockHttpResponseBuilderImpl implements MockHttpResponseBuilder, MockHttpRequestCallback {

		/**
		 * Delegate {@link HttpResponse}.
		 */
		private final ProcessAwareHttpResponse<ByteBuffer> delegate;

		/**
		 * {@link MockHttpResponse}.
		 */
		private MockHttpResponse response = null;

		/**
		 * Instantiate.
		 */
		private MockHttpResponseBuilderImpl() {
			MockStreamBufferPool bufferPool = new MockStreamBufferPool();
			this.delegate = new ProcessAwareHttpResponse<>(HttpVersion.HTTP_1_1, bufferPool, true,
					new MockProcessAwareContext(), new MockHttpResponseWriter(this, null));
		}

		/*
		 * =============== MockHttpResponseBuilder ==================
		 */

		@Override
		public HttpVersion getHttpVersion() {
			return this.delegate.getHttpVersion();
		}

		@Override
		public void setHttpVersion(HttpVersion version) {
			this.delegate.setHttpVersion(version);
		}

		@Override
		public HttpStatus getHttpStatus() {
			return this.delegate.getHttpStatus();
		}

		@Override
		public void setHttpStatus(HttpStatus status) {
			this.delegate.setHttpStatus(status);
		}

		@Override
		public HttpResponseHeaders getHttpHeaders() {
			return this.delegate.getHttpHeaders();
		}

		@Override
		public void setContentType(String contentType, Charset charset) throws IOException {
			this.delegate.setContentType(contentType, charset);
		}

		@Override
		public void setContentType(HttpHeaderValue contentTypeAndCharsetValue, Charset charset) throws IOException {
			this.delegate.setContentType(contentTypeAndCharsetValue, charset);
		}

		@Override
		public String getContentType() {
			return this.delegate.getContentType();
		}

		@Override
		public Charset getContentCharset() {
			return this.delegate.getContentCharset();
		}

		@Override
		public ServerOutputStream getEntity() throws IOException {
			return this.delegate.getEntity();
		}

		@Override
		public ServerWriter getEntityWriter() throws IOException {
			return this.delegate.getEntityWriter();
		}

		@Override
		public void setEscalationHandler(HttpEscalationHandler escalationHandler) {
			this.delegate.setEscalationHandler(escalationHandler);
		}

		@Override
		public void reset() throws IOException {
			this.delegate.reset();
		}

		@Override
		public void send() throws IOException {
			this.delegate.send();
		}

		@Override
		public MockHttpResponse build() {
			try {
				// Write the response (to obtain mock HTTP response)
				this.delegate.flushResponseToHttpResponseWriter(null);
			} catch (IOException ex) {
				this.response = new MockHttpResponseImpl(ex);
			}

			// Return the built response
			return this.response;
		}

		/*
		 * ================= MockHttpRequestCallback ==================
		 */

		@Override
		public void response(MockHttpResponse response) {
			// Capture response for build
			this.response = response;
		}
	}

	/**
	 * {@link MockHttpResponse} implementation.
	 */
	private static class MockHttpResponseImpl implements MockHttpResponse {

		/**
		 * {@link HttpVersion}.
		 */
		private final HttpVersion version;

		/**
		 * {@link HttpStatus}.
		 */
		private final HttpStatus status;

		/**
		 * {@link HttpHeader} instances.
		 */
		private final List<WritableHttpHeader> headers;

		/**
		 * HTTP entity {@link InputStream}.
		 */
		private final InputStream entityInputStream;

		/**
		 * Possible failure in writing the response.
		 */
		private final Throwable failure;

		/**
		 * Loads the response.
		 * 
		 * @param version
		 *            {@link HttpVersion}.
		 * @param status
		 *            {@link HttpStatus}.
		 * @param headers
		 *            {@link List} of {@link HttpHeader} instances.
		 * @param entityInputStream
		 *            HTTP entity {@link InputStream}.
		 */
		private MockHttpResponseImpl(HttpVersion version, HttpStatus status, List<WritableHttpHeader> headers,
				InputStream entityInputStream) {
			this.version = version;
			this.status = status;
			this.headers = headers;
			this.entityInputStream = entityInputStream;
			this.failure = null;
		}

		/**
		 * Loads with response failure.
		 * 
		 * @param version
		 *            {@link HttpVersion}.
		 * @param status
		 *            {@link HttpStatus}.
		 * @param headers
		 *            {@link List} of {@link HttpHeader} instances.
		 * @param entityInputStream
		 *            HTTP entity {@link InputStream}.
		 */
		private MockHttpResponseImpl(Throwable failure) {
			this.failure = failure;
			this.version = null;
			this.status = null;
			this.headers = null;
			this.entityInputStream = null;
		}

		/**
		 * Ensures no failure in writing the response.
		 */
		private void ensureNoFailure() {
			if (this.failure != null) {
				throw OfficeFrameTestCase.fail(this.failure);
			}
		}

		/*
		 * =============== MockHttpResponse =======================
		 */

		@Override
		public HttpVersion getHttpVersion() {
			this.ensureNoFailure();
			return this.version;
		}

		@Override
		public HttpStatus getHttpStatus() {
			this.ensureNoFailure();
			return this.status;
		}

		@Override
		public WritableHttpHeader getFirstHeader(String name) {
			for (WritableHttpHeader header : this.headers) {
				if (name.equalsIgnoreCase(header.getName())) {
					return header; // found
				}
			}

			// As here, not found
			return null;
		}

		@Override
		public List<WritableHttpHeader> getHttpHeaders() {
			this.ensureNoFailure();
			return this.headers;
		}

		@Override
		public InputStream getHttpEntity() {
			this.ensureNoFailure();
			return this.entityInputStream;
		}

		@Override
		public String getHttpEntity(Charset charset) {
			this.ensureNoFailure();

			// Ensure have charset
			if (charset == null) {
				charset = ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET;
			}

			// Read in the contents
			StringWriter writer = new StringWriter();
			try {
				InputStreamReader reader = new InputStreamReader(this.entityInputStream, charset);
				for (int character = reader.read(); character != -1; character = reader.read()) {
					writer.write(character);
				}
				writer.flush();
			} catch (IOException ex) {
				throw OfficeFrameTestCase.fail(ex);
			}

			// Return the entity as text
			return writer.toString();
		}
	}

	/**
	 * {@link MockServerHttpConnection} implementation.
	 */
	public static class MockServerHttpConnectionImpl implements MockServerHttpConnection, MockHttpRequestCallback {

		/**
		 * Delegate {@link ServerHttpConnection}.
		 */
		private final ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> delegate;

		/**
		 * {@link MockHttpResponse}.
		 */
		private MockHttpResponse response = null;

		/**
		 * Instantiate.
		 * 
		 * @param request
		 *            {@link MockHttpRequestBuilder}.
		 */
		public MockServerHttpConnectionImpl(MockHttpRequestBuilder request) {
			this.delegate = createServerHttpConnection(request, new MockHttpServer(), this);
			this.delegate.setProcessAwareContext(new MockProcessAwareContext());
		}

		/*
		 * ================= MockServerHttpConnection ==================
		 */

		@Override
		public MockHttpResponse send(Throwable escalation) {

			// Send the response
			try {
				this.delegate.getServiceFlowCallback().run(escalation);
			} catch (Throwable ex) {
				throw OfficeFrameTestCase.fail(ex);
			}

			// Return the response
			return this.response;
		}

		/*
		 * ================= MockHttpRequestCallback ===================
		 */

		@Override
		public void response(MockHttpResponse response) {
			this.response = response;
		}

		/*
		 * =================== ServerHttpConnection ====================
		 */

		@Override
		public HttpRequest getHttpRequest() {
			return this.delegate.getHttpRequest();
		}

		@Override
		public HttpResponse getHttpResponse() {
			return this.delegate.getHttpResponse();
		}

		@Override
		public boolean isSecure() {
			return this.delegate.isSecure();
		}

		@Override
		public HttpServerLocation getHttpServerLocation() {
			return this.delegate.getHttpServerLocation();
		}

		@Override
		public Serializable exportState() throws IOException {
			return this.delegate.exportState();
		}

		@Override
		public void importState(Serializable momento) throws IllegalArgumentException, IOException {
			this.delegate.importState(momento);
		}

		@Override
		public HttpMethod getClientHttpMethod() {
			return this.delegate.getClientHttpMethod();
		}

		@Override
		public HttpRequestHeaders getClientHttpHeaders() {
			return this.delegate.getClientHttpHeaders();
		}
	}

	/**
	 * {@link MockHttpRequestCallback} to enable synchronous invocation.
	 */
	private static class SynchronousMockHttpRequestCallback implements MockHttpRequestCallback {

		/**
		 * {@link MockHttpResponse}.
		 */
		private MockHttpResponse response = null;

		/**
		 * Wait for the {@link MockHttpResponse}.
		 * 
		 * @param maxWaitTimeInMilliseconds
		 *            Max wait time in milliseconds.
		 * @return {@link MockHttpResponse}.
		 */
		private synchronized MockHttpResponse waitForResponse(int maxWaitTimeInMilliseconds) {

			// Wait for the response
			try {
				long startTime = System.currentTimeMillis();
				while (this.response == null) {

					// Determine if timed out
					long runTimeInSeconds = (System.currentTimeMillis() - startTime);
					if (runTimeInSeconds > maxWaitTimeInMilliseconds) {
						throw new Error("Timed out waiting for " + MockHttpResponse.class.getSimpleName());
					}

					// Wait some time for response
					this.wait(10);
				}
			} catch (InterruptedException ex) {
				throw new Error(ex);
			}

			// Return the response
			return this.response;
		}

		/*
		 * ==================== MockHttpRequestCallback =======================
		 */

		@Override
		public synchronized void response(MockHttpResponse response) {

			// Specify response
			this.response = response;

			// Notify have response
			this.notify();
		}
	}

}