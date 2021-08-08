/*-
 * #%L
 * HTTP Server
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

package net.officefloor.server.http.impl;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.function.Supplier;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequestCookies;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.impl.ByteArrayByteSequence;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * Tests the {@link MaterialisingHttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class MaterialisingHttpRequestTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpMethod} {@link Supplier}.
	 */
	@SuppressWarnings("unchecked")
	private final Supplier<HttpMethod> methodSupplier = this.createMock(Supplier.class);

	/**
	 * Request URI {@link Supplier}.
	 */
	@SuppressWarnings("unchecked")
	private final Supplier<String> requestUriSupplier = this.createMock(Supplier.class);

	/**
	 * {@link HttpVersion}.
	 */
	private final HttpVersion version = HttpVersion.HTTP_1_1;

	/**
	 * {@link HttpRequestHeaders}.
	 */
	private final HttpRequestHeaders headers = this.createMock(HttpRequestHeaders.class);

	/**
	 * {@link HttpRequestCookies}.
	 */
	private final HttpRequestCookies cookies = this.createMock(HttpRequestCookies.class);

	/**
	 * {@link ByteSequence}.
	 */
	private final ByteSequence entity = new ByteArrayByteSequence("TEST".getBytes(ServerHttpConnection.HTTP_CHARSET));

	/**
	 * {@link MaterialisingHttpRequest} to test.
	 */
	private final MaterialisingHttpRequest request = new MaterialisingHttpRequest(this.methodSupplier,
			this.requestUriSupplier, this.version, this.headers, this.cookies, this.entity);

	/**
	 * Ensure can obtain {@link HttpMethod}.
	 */
	public void testHttpMethod() {
		HttpMethod method = new HttpMethod("TEST");
		this.recordReturn(this.methodSupplier, this.methodSupplier.get(), method);
		this.replayMockObjects();
		assertSame("Incorrect HTTP method", method, this.request.getMethod());
		assertSame("Should now cache HTTP method", method, this.request.getMethod());
		this.verifyMockObjects();
	}

	/**
	 * Ensure can obtain Request URI.
	 */
	public void testRequestUri() {
		String requestUri = "/test";
		this.recordReturn(this.requestUriSupplier, this.requestUriSupplier.get(), requestUri);
		this.replayMockObjects();
		assertSame("Incorrect request URI", requestUri, this.request.getUri());
		assertSame("Should now cache request URI", requestUri, this.request.getUri());
		this.verifyMockObjects();
	}

	/**
	 * Ensure can obtain {@link HttpVersion}.
	 */
	public void testHttpVersion() {
		this.replayMockObjects();
		assertSame("Incorrect HTTP version", this.version, this.request.getVersion());
		assertSame("Should continue to be same HTTP version", this.version, this.request.getVersion());
		this.verifyMockObjects();
	}

	/**
	 * Ensure can obtain {@link HttpRequestHeaders}.
	 */
	public void testHttpRequestHeaders() {
		this.replayMockObjects();
		assertSame("Incorrect HTTP headers", this.headers, this.request.getHeaders());
		this.verifyMockObjects();
	}

	/**
	 * Ensure can read in the HTTP entity.
	 */
	public void testHttpEntity() throws Exception {
		this.replayMockObjects();
		InputStreamReader reader = new InputStreamReader(this.request.getEntity(), ServerHttpConnection.HTTP_CHARSET);
		StringWriter buffer = new StringWriter();
		for (int character = reader.read(); character >= 0; character = reader.read()) {
			buffer.write(character);
		}
		this.verifyMockObjects();
		assertEquals("Incorrect HTTP entity", "TEST", buffer.toString());
	}

}
