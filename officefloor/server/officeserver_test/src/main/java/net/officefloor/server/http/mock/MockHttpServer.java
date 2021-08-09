/*-
 * #%L
 * Testing of HTTP Server
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
import net.officefloor.server.http.HttpEscalationHandler;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestCookie;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseCookie;
import net.officefloor.server.http.HttpResponseCookies;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.WritableHttpCookie;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.http.impl.MaterialisingHttpRequest;
import net.officefloor.server.http.impl.MaterialisingHttpRequestCookies;
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
import net.officefloor.test.JUnitAgnosticAssert;

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
	 * @param input {@link DeployedOfficeInput}.
	 * @return {@link MockHttpServer} to send {@link HttpRequest} instances.
	 * @throws Exception If fails to configure {@link MockHttpServer}.
	 */
	public static MockHttpServer configureMockHttpServer(DeployedOfficeInput input) throws Exception {
		MockHttpServer httpServer = new MockHttpServer();
		configureMockHttpServer(httpServer, input);
		return httpServer;
	}

	/**
	 * Enables extending implementations to configure themselves as the
	 * {@link MockHttpServer}.
	 * 
	 * @param httpServer {@link MockHttpServer}.
	 * @param input      {@link DeployedOfficeInput}.
	 * @throws Exception If fails to configure {@link MockHttpServer}.
	 */
	protected static void configureMockHttpServer(MockHttpServer httpServer, DeployedOfficeInput input)
			throws Exception {
		new HttpServer(httpServer, httpServer, null, null, true, null, input, null, null);
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
	 * @param requestUri Request URI.
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
		return new MockHttpResponseBuilderImpl((MockHttpRequestBuilderImpl) mockRequest("/mock"), new MockHttpServer());
	}

	/**
	 * Creates a mock {@link HttpResponseCookie}.
	 *
	 * @param name  Name.
	 * @param value value.
	 * @return {@link HttpResponseCookie}.
	 */
	public static WritableHttpCookie mockResponseCookie(String name, String value) {
		return new WritableHttpCookie(name, value, new MockManagedObjectContext());
	}

	/**
	 * Convenience method to create the {@link MockServerHttpConnection} for GET /
	 * with no headers nor entity.
	 * 
	 * @return {@link MockServerHttpConnection}.
	 */
	public static MockServerHttpConnection mockConnection() {
		return new MockServerHttpConnectionImpl(mockRequest());
	}

	/**
	 * Creates the {@link MockServerHttpConnection}.
	 * 
	 * @param request {@link MockHttpRequestBuilder} for the {@link HttpRequest} of
	 *                the {@link MockServerHttpConnection}.
	 * @return {@link MockServerHttpConnection}.
	 */
	public static MockServerHttpConnection mockConnection(MockHttpRequestBuilder request) {
		return new MockServerHttpConnectionImpl(request);
	}

	/**
	 * Creates the {@link ProcessAwareServerHttpConnectionManagedObject}.
	 * 
	 * @param request        {@link MockHttpRequestBuilder}.
	 * @param server         {@link MockHttpServer}.
	 * @param serverLocation {@link HttpServerLocation}.
	 * @param callback       {@link MockHttpRequestCallback}.
	 * @return {@link ProcessAwareServerHttpConnectionManagedObject}.
	 */
	private static ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> createServerHttpConnection(
			MockHttpRequestBuilder request, MockHttpServer server, HttpServerLocation serverLocation,
			MockHttpRequestCallback callback) {

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
		HttpResponseWriter<ByteBuffer> responseWriter = new MockHttpResponseWriter(impl, server, callback,
				checkBufferPool);

		// Create the server HTTP connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<>(
				serverLocation, isSecure, methodSupplier, requestUriSupplier, requestVersion, requestHeaders,
				requestEntity, null, null, true, responseWriter, bufferPool);

		// Return the connection
		return connection;
	}

	/**
	 * {@link ExternalServiceInput}.
	 */
	private ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>> serviceInput;

	/**
	 * Timeout in milliseconds for synchronous send.
	 */
	private int timeout = 3000; // allow reasonably generous time for busy build servers

	/**
	 * Instantiated via static methods or extending.
	 */
	protected MockHttpServer() {
	}

	/**
	 * Specifies the timeout for synchronous send.
	 * 
	 * @param timeout Timeout in milliseconds.
	 * @return <code>this</code>.
	 */
	public MockHttpServer timeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * Sends the {@link MockHttpRequestBuilder}.
	 * 
	 * @param request  {@link MockHttpRequestBuilder}.
	 * @param callback {@link MockHttpRequestCallback} to receive the
	 *                 {@link MockHttpResponse}.
	 */
	public void send(MockHttpRequestBuilder request, MockHttpRequestCallback callback) {

		// Create the server HTTP connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = createServerHttpConnection(request, this,
				this, callback);

		// Service the request
		this.serviceInput.service(connection, connection.getServiceFlowCallback());
	}

	/**
	 * {@link HttpResponseWriter} to write the {@link MockHttpResponse}.
	 */
	private static class MockHttpResponseWriter implements HttpResponseWriter<ByteBuffer> {

		/**
		 * {@link MockHttpRequestBuilderImpl}.
		 */
		private final MockHttpRequestBuilderImpl request;

		/**
		 * {@link MockHttpServer}.
		 */
		private final MockHttpServer server;

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
		 * @param request    {@link MockHttpRequestBuilderImpl}.
		 * @param server     {@link MockHttpServer}.
		 * @param callback   {@link MockHttpRequestCallback}.
		 * @param bufferPool {@link MockStreamBufferPool} to check all
		 *                   {@link StreamBuffer} instances are released on writing
		 *                   {@link HttpResponse}. May be <code>null</code>.
		 */
		private MockHttpResponseWriter(MockHttpRequestBuilderImpl request, MockHttpServer server,
				MockHttpRequestCallback callback, MockStreamBufferPool bufferPool) {
			this.request = request;
			this.server = server;
			this.callback = callback;
			this.bufferPool = bufferPool;
		}

		/*
		 * ================== HttpResponseWriter ==================
		 */

		@Override
		public void writeHttpResponse(HttpVersion version, HttpStatus status, WritableHttpHeader headHttpHeader,
				WritableHttpCookie headHttpCookie, long contentLength, HttpHeaderValue contentType,
				StreamBuffer<ByteBuffer> contentHeadStreamBuffer) {
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

				// Obtain the listing of response HTTP cookies
				List<WritableHttpCookie> cookies = new ArrayList<>();
				while (headHttpCookie != null) {
					cookies.add(headHttpCookie);
					headHttpCookie = headHttpCookie.next;
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
				MockHttpResponse response = this.server.createMockHttpResponse(this.request, version, status, headers,
						cookies, new ByteArrayInputStream(responseEntity));

				// Response received
				this.callback.response(response);

			} catch (Throwable ex) {
				// Provide failed HTTP response
				this.callback.response(this.server.createMockHttpResponse(this.request, ex));
			}
		}
	}

	/**
	 * Sends the {@link MockHttpRequestBuilder} and blocks waiting for the
	 * {@link MockHttpResponse}.
	 * 
	 * @param request {@link MockHttpRequestBuilder}.
	 * @return {@link MockHttpResponse}.
	 */
	public MockHttpResponse send(MockHttpRequestBuilder request) {

		// Create the synchronous callback
		SynchronousMockHttpRequestCallback callback = new SynchronousMockHttpRequestCallback();

		// Undertake the request
		this.send(request, callback);

		// Block waiting for response
		return callback.waitForResponse(this.timeout);
	}

	/**
	 * Sends the {@link MockHttpRequestBuilder} and blocks following the redirect.
	 * 
	 * @param request {@link MockHttpRequestBuilder}.
	 * @return Redirect {@link MockHttpResponse}.
	 */
	public MockHttpResponse sendFollowRedirect(MockHttpRequestBuilder request) {

		// Create the synchronous callback
		SynchronousMockHttpRequestCallback callback = new SynchronousMockHttpRequestCallback();
		this.send(request, callback);
		MockHttpResponse response = callback.waitForResponse(this.timeout);

		// Ensure redirect
		JUnitAgnosticAssert.assertEquals(HttpStatus.SEE_OTHER, response.getStatus(),
				"Initial response was not redirect");

		// Capture whether initial request is secure
		MockHttpRequestBuilderImpl impl = (MockHttpRequestBuilderImpl) request;

		// Undertake the redirect
		callback = new SynchronousMockHttpRequestCallback();
		String location = response.getHeader("location").getValue();
		this.send(MockHttpServer.mockRequest(location).secure(impl.isSecure).cookies(response), callback);
		return callback.waitForResponse(this.timeout);
	}

	/**
	 * Creates the {@link MockHttpResponse}.
	 * 
	 * @param request           {@link MockHttpRequest}.
	 * @param version           {@link HttpVersion}.
	 * @param status            {@link HttpStatus}.
	 * @param headers           {@link WritableHttpHeader} instances.
	 * @param cookies           {@link WritableHttpCookie} instances.
	 * @param entityInputStream Entity.
	 * @return {@link MockHttpResponse}.
	 */
	protected MockHttpResponse createMockHttpResponse(MockHttpRequest request, HttpVersion version, HttpStatus status,
			List<WritableHttpHeader> headers, List<WritableHttpCookie> cookies, InputStream entityInputStream) {
		return new MockHttpResponseImpl(request, version, status, headers, cookies, entityInputStream);
	}

	/**
	 * Creates the {@link MockHttpResponse}.
	 * 
	 * @param request {@link MockHttpRequest}.
	 * @param failure Failure in servicing.
	 * @return {@link MockHttpResponse}.
	 */
	protected MockHttpResponse createMockHttpResponse(MockHttpRequest request, Throwable failure) {
		return new MockHttpResponseImpl(request, failure);
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
		return (isSecure ? "https" : "http") + "://" + this.getDomain() + path;
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
	private static class MockHttpRequestBuilderImpl
			implements MockHttpRequestBuilder, NonMaterialisedHttpHeaders, MockHttpRequest {

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
		private final List<NonMaterialisedHttpHeader> headers = new LinkedList<>();

		/**
		 * {@link MockNonMaterialisedHttpCookie}.
		 */
		private MockNonMaterialisedHttpCookie cookie = null;

		/**
		 * HTTP entity.
		 */
		private final ByteArrayOutputStream entity = new ByteArrayOutputStream();

		/**
		 * Indicates if running stress test for request.
		 */
		private boolean isStress = false;

		/**
		 * Obtains the {@link MockNonMaterialisedHttpCookie}.
		 * 
		 * @return {@link MockNonMaterialisedHttpCookie}.
		 */
		private MockNonMaterialisedHttpCookie getCookieHeader() {
			if (this.cookie == null) {
				this.cookie = new MockNonMaterialisedHttpCookie(this);
				this.headers.add(this.cookie);
			}
			return this.cookie;
		}

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
		public MockHttpRequestBuilder cookie(String name, String value) {
			this.getCookieHeader().cookies.add(new MockCookie(name, value));
			return this;
		}

		@Override
		public MockHttpRequestBuilder cookies(MockHttpResponse cookies) {
			this.getCookieHeader().responses.add(cookies);
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
				return JUnitAgnosticAssert.fail(ex);
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
			final MaterialisingHttpRequestCookies cookies = new MaterialisingHttpRequestCookies(headers);
			final ByteArrayByteSequence entity = new ByteArrayByteSequence(this.entity.toByteArray());
			return new MaterialisingHttpRequest(() -> method, () -> requestUri, this.version, headers, cookies, entity);
		}

		/*
		 * ================== NonMaterialisedHttpHeaders =====================
		 */

		@Override
		public Iterator<NonMaterialisedHttpHeader> iterator() {
			return this.headers.iterator();
		}

		@Override
		public int length() {
			return this.headers.size();
		}

		/*
		 * ======================= MockHttpRequest ===========================
		 */

		@Override
		public String getRequestUri() {
			return this.requestUri;
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
		 * @param name  {@link HttpHeader} name.
		 * @param value {@link HttpHeader} value.
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
	 * {@link NonMaterialisedHttpHeader} containing the {@link HttpRequestCookie}
	 * instances.
	 */
	private static class MockNonMaterialisedHttpCookie implements NonMaterialisedHttpHeader {

		/**
		 * {@link MockHttpRequestBuilderImpl}.
		 */
		private final MockHttpRequestBuilderImpl request;

		/**
		 * {@link MockCookie} instances.
		 */
		private final List<MockCookie> cookies = new LinkedList<>();

		/**
		 * Listing of {@link MockHttpResponse} instances to have their appropriate
		 * {@link HttpResponseCookie} instances added as a {@link HttpRequestCookie}.
		 */
		private final List<MockHttpResponse> responses = new LinkedList<>();

		/**
		 * Instantiate.
		 * 
		 * @param request {@link MockHttpRequestBuilderImpl}.
		 */
		private MockNonMaterialisedHttpCookie(MockHttpRequestBuilderImpl request) {
			this.request = request;
		}

		/*
		 * ================= NonMaterialisedHttpHeader ========================
		 */

		@Override
		public CharSequence getName() {
			return "cookie";
		}

		@Override
		public HttpHeader materialiseHttpHeader() {
			return new MockCookieHttpHeader(this);
		}
	}

	/**
	 * Mock Cookie.
	 */
	private static class MockCookie {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Value.
		 */
		private final String value;

		/**
		 * Instantiate.
		 * 
		 * @param name  Name.
		 * @param value Value.
		 */
		private MockCookie(String name, String value) {
			this.name = name;
			this.value = value;
		}
	}

	/**
	 * {@link HttpHeader} containing the {@link HttpRequestCookie} instances.
	 */
	private static class MockCookieHttpHeader implements HttpHeader {

		/**
		 * {@link MockNonMaterialisedHttpCookie}.
		 */
		private final MockNonMaterialisedHttpCookie header;

		/**
		 * Instantiate.
		 * 
		 * @param header {@link MockNonMaterialisedHttpCookie}.
		 */
		private MockCookieHttpHeader(MockNonMaterialisedHttpCookie header) {
			this.header = header;
		}

		/*
		 * ======================== HttpHeader ================================
		 */

		@Override
		public String getName() {
			return "cookie";
		}

		@Override
		public String getValue() {
			StringBuilder value = new StringBuilder();

			// Load the added cookies
			for (MockCookie cookie : this.header.cookies) {
				value.append(cookie.name);
				value.append("=");
				value.append(cookie.value);
				value.append(";");
			}

			// Load appropriate cookies (based on path)
			for (MockHttpResponse response : this.header.responses) {
				for (WritableHttpCookie cookie : response.getCookies()) {
					String path = cookie.getPath();
					if ((path == null) || (this.header.request.requestUri.startsWith(path))) {
						value.append(cookie.getName());
						value.append("=");
						value.append(cookie.getValue());
						value.append(";");
					}
				}
			}

			// Return the value
			return value.toString();
		}
	}

	/**
	 * {@link MockHttpResponseBuilder} implementation.
	 */
	private static class MockHttpResponseBuilderImpl implements MockHttpResponseBuilder, MockHttpRequestCallback {

		/**
		 * {@link MockHttpRequestBuilderImpl}.
		 */
		private final MockHttpRequestBuilderImpl request;

		/**
		 * {@link MockHttpServer}.
		 */
		private final MockHttpServer server;

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
		 * 
		 * @param request {@link MockHttpRequestBuilderImpl} for the
		 *                {@link MockHttpResponse}.
		 * @param server  {@link MockHttpServer}.
		 */
		private MockHttpResponseBuilderImpl(MockHttpRequestBuilderImpl request, MockHttpServer server) {
			this.request = request;
			this.server = server;
			MockStreamBufferPool bufferPool = new MockStreamBufferPool();
			HttpServerLocation serverLocation = new MockHttpServer();
			ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> serverHttpConnection = new ProcessAwareServerHttpConnectionManagedObject<>(
					serverLocation, false, () -> HttpMethod.GET, () -> "/", HttpVersion.HTTP_1_1, null, null, null,
					null, true, new MockHttpResponseWriter(this.request, this.server, this, null), bufferPool);
			this.delegate = new ProcessAwareHttpResponse<>(serverHttpConnection, HttpVersion.HTTP_1_1,
					new MockManagedObjectContext());
		}

		/*
		 * =============== MockHttpResponseBuilder ==================
		 */

		@Override
		public HttpVersion getVersion() {
			return this.delegate.getVersion();
		}

		@Override
		public void setVersion(HttpVersion version) {
			this.delegate.setVersion(version);
		}

		@Override
		public HttpStatus getStatus() {
			return this.delegate.getStatus();
		}

		@Override
		public void setStatus(HttpStatus status) {
			this.delegate.setStatus(status);
		}

		@Override
		public HttpResponseHeaders getHeaders() {
			return this.delegate.getHeaders();
		}

		@Override
		public HttpResponseCookies getCookies() {
			return this.delegate.getCookies();
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
		public HttpEscalationHandler getEscalationHandler() {
			return this.delegate.getEscalationHandler();
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
				this.response = this.server.createMockHttpResponse(this.request, ex);
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
	 * HTTP request details for the {@link MockHttpResponse}.
	 */
	protected static interface MockHttpRequest {

		/**
		 * Obtains the request URI.
		 * 
		 * @return Request URI.
		 */
		String getRequestUri();
	}

	/**
	 * {@link MockHttpResponse} implementation.
	 */
	protected static class MockHttpResponseImpl implements MockHttpResponse {

		/**
		 * {@link MockHttpRequest} for this {@link MockHttpResponse}.
		 */
		protected final MockHttpRequest request;

		/**
		 * {@link HttpVersion}.
		 */
		protected final HttpVersion version;

		/**
		 * {@link HttpStatus}.
		 */
		protected final HttpStatus status;

		/**
		 * {@link HttpHeader} instances.
		 */
		protected final List<WritableHttpHeader> headers;

		/**
		 * {@link HttpResponseCookie} instances.
		 */
		protected final List<WritableHttpCookie> cookies;

		/**
		 * HTTP entity {@link InputStream}.
		 */
		protected final InputStream entityInputStream;

		/**
		 * Possible failure in writing the response.
		 */
		protected final Throwable failure;

		/**
		 * Loads the response.
		 *
		 * @param request           {@link MockHttpRequest} for this
		 *                          {@link MockHttpResponse}.
		 * @param version           {@link HttpVersion}.
		 * @param status            {@link HttpStatus}.
		 * @param headers           {@link List} of {@link HttpHeader} instances.
		 * @param cookies           {@link List} of {@link HttpResponseCookie}
		 *                          instances.
		 * @param entityInputStream HTTP entity {@link InputStream}.
		 */
		protected MockHttpResponseImpl(MockHttpRequest request, HttpVersion version, HttpStatus status,
				List<WritableHttpHeader> headers, List<WritableHttpCookie> cookies, InputStream entityInputStream) {
			this.request = request;
			this.version = version;
			this.status = status;
			this.headers = headers;
			this.cookies = cookies;
			this.entityInputStream = entityInputStream;
			this.failure = null;
		}

		/**
		 * Loads with response failure.
		 * 
		 * @param request {@link MockHttpRequest} for this {@link MockHttpResponse}.
		 * @param failure {@link Throwable}.
		 */
		protected MockHttpResponseImpl(MockHttpRequest request, Throwable failure) {
			this.request = request;
			this.failure = failure;
			this.version = null;
			this.status = null;
			this.headers = null;
			this.cookies = null;
			this.entityInputStream = null;
		}

		/**
		 * Obtains the details of the {@link HttpRequest}.
		 * 
		 * @return Details of the {@link HttpRequest}.
		 */
		private String getRequestInfo() {
			return this.request.getRequestUri();
		}

		/**
		 * Ensures no failure in writing the response.
		 */
		private void ensureNoFailure() {
			if (this.failure != null) {
				JUnitAgnosticAssert.fail(this.failure);
			}
		}

		/*
		 * =============== MockHttpResponse =======================
		 */

		@Override
		public HttpVersion getVersion() {
			this.ensureNoFailure();
			return this.version;
		}

		@Override
		public HttpStatus getStatus() {
			this.ensureNoFailure();
			return this.status;
		}

		@Override
		public WritableHttpHeader getHeader(String name) {
			for (WritableHttpHeader header : this.headers) {
				if (name.equalsIgnoreCase(header.getName())) {
					return header; // found
				}
			}

			// As here, not found
			return null;
		}

		@Override
		public void assertHeader(String name, String value) {
			WritableHttpHeader header = this.getHeader(name);
			if (header == null) {
				throw new AssertionError("No header by name '" + name + "' for " + this.getRequestInfo());
			}
			JUnitAgnosticAssert.assertEquals(value, header.getValue(),
					"Incorrect value for header " + name + " for " + this.getRequestInfo());
		}

		@Override
		public List<WritableHttpHeader> getHeaders() {
			this.ensureNoFailure();
			return this.headers;
		}

		@Override
		public WritableHttpCookie getCookie(String name) {
			for (WritableHttpCookie cookie : this.cookies) {
				if (name.equals(cookie.getName())) {
					return cookie; // found
				}
			}

			// As here, not found
			return null;
		}

		@Override
		public List<WritableHttpCookie> getCookies() {
			this.ensureNoFailure();
			return this.cookies;
		}

		@Override
		public void assertCookie(HttpResponseCookie cookie) {
			WritableHttpCookie writable = (WritableHttpCookie) cookie;
			String name = cookie.getName();
			WritableHttpCookie actual = this.getCookie(name);
			JUnitAgnosticAssert.assertNotNull(actual, "No cookie by name '" + name + "' for " + this.getRequestInfo());
			JUnitAgnosticAssert.assertEquals(writable.toResponseHeaderValue(), actual.toResponseHeaderValue(),
					"Incorrect cookie " + name + " for " + this.getRequestInfo());
		}

		@Override
		public InputStream getEntity() {
			this.ensureNoFailure();
			return this.entityInputStream;
		}

		@Override
		public String getEntity(Charset charset) {
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
				return JUnitAgnosticAssert.fail(ex);
			}

			// Return the entity as text
			return writer.toString();
		}

		@Override
		public void assertStatus(int statusCode) {
			JUnitAgnosticAssert.assertEquals(statusCode, this.getStatus().getStatusCode(),
					"Incorrect status for " + this.getRequestInfo());
		}

		@Override
		public void assertStatus(HttpStatus status) {
			assertStatus(status.getStatusCode());
		}

		@Override
		public void assertResponse(int statusCode, String entity, String... headerNameValuePairs) {
			String actualEntity = this.getEntity(null);
			JUnitAgnosticAssert.assertEquals(statusCode, this.getStatus().getStatusCode(), "Incorrect status for "
					+ this.getRequestInfo() + ": " + ("".equals(actualEntity) ? "[empty]" : actualEntity));
			JUnitAgnosticAssert.assertEquals(entity, actualEntity, "Incorrect entity for " + this.getRequestInfo());
			for (int i = 0; i < headerNameValuePairs.length; i += 2) {
				String name = headerNameValuePairs[i];
				String value = headerNameValuePairs[i + 1];
				this.assertHeader(name, value);
			}
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
		 * @param request {@link MockHttpRequestBuilder}.
		 */
		public MockServerHttpConnectionImpl(MockHttpRequestBuilder request) {
			MockHttpServer server = new MockHttpServer();
			this.delegate = createServerHttpConnection(request, server, server, this);
			this.delegate.setManagedObjectContext(new MockManagedObjectContext());
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
				return JUnitAgnosticAssert.fail(ex);
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
		public HttpRequest getRequest() {
			return this.delegate.getRequest();
		}

		@Override
		public HttpResponse getResponse() {
			return this.delegate.getResponse();
		}

		@Override
		public boolean isSecure() {
			return this.delegate.isSecure();
		}

		@Override
		public HttpServerLocation getServerLocation() {
			return this.delegate.getServerLocation();
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
		public HttpRequest getClientRequest() {
			return this.delegate.getClientRequest();
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
		 * @param maxWaitTimeInMilliseconds Max wait time in milliseconds.
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
						throw new Error("Timed out waiting for " + MockHttpResponse.class.getSimpleName() + " (waited "
								+ maxWaitTimeInMilliseconds + " milliseconds)");
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
