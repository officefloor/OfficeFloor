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

import java.util.function.Supplier;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestCookies;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.stream.ServerInputStream;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ByteSequenceServerInputStream;

/**
 * {@link HttpRequest} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class MaterialisingHttpRequest implements HttpRequest {

	/**
	 * Supplies the {@link HttpMethod}.
	 */
	private final Supplier<HttpMethod> methodSupplier;

	/**
	 * {@link HttpMethod}.
	 */
	private HttpMethod method = null;

	/**
	 * Supplies the request URI.
	 */
	private final Supplier<String> requestUriSupplier;

	/**
	 * Request URI.
	 */
	private String requestUri = null;

	/**
	 * {@link HttpVersion}.
	 */
	private final HttpVersion version;

	/**
	 * {@link HttpRequestHeaders}.
	 */
	private final HttpRequestHeaders headers;

	/**
	 * {@link HttpRequestCookies}.
	 */
	private final HttpRequestCookies cookies;

	/**
	 * {@link ByteSequence} for the HTTP entity.
	 */
	private final ByteSequence entity;

	/**
	 * {@link ByteSequenceServerInputStream}.
	 */
	private final ByteSequenceServerInputStream entityStream;

	/**
	 * Instantiate.
	 * 
	 * @param methodSupplier
	 *            {@link Supplier} for the {@link HttpMethod}.
	 * @param requestUriSupplier
	 *            {@link Supplier} for the request URI.
	 * @param version
	 *            {@link HttpVersion}.
	 * @param headers
	 *            {@link HttpRequestHeaders}.
	 * @param cookies
	 *            {@link HttpRequestCookies}.
	 * @param entity
	 *            {@link ByteSequence} for the HTTP entity.
	 */
	public MaterialisingHttpRequest(Supplier<HttpMethod> methodSupplier, Supplier<String> requestUriSupplier,
			HttpVersion version, HttpRequestHeaders headers, HttpRequestCookies cookies, ByteSequence entity) {
		this.methodSupplier = methodSupplier;
		this.requestUriSupplier = requestUriSupplier;
		this.version = version;
		this.headers = headers;
		this.cookies = cookies;
		this.entity = entity;
		this.entityStream = new ByteSequenceServerInputStream(this.entity, 0);
	}

	/*
	 * ================= HttpRequest =====================
	 */

	@Override
	public HttpMethod getMethod() {
		if (this.method == null) {
			this.method = this.methodSupplier.get();
		}
		return this.method;
	}

	@Override
	public String getUri() {
		if (this.requestUri == null) {
			this.requestUri = this.requestUriSupplier.get();
		}
		return this.requestUri;
	}

	@Override
	public HttpVersion getVersion() {
		return this.version;
	}

	@Override
	public HttpRequestHeaders getHeaders() {
		return this.headers;
	}

	@Override
	public HttpRequestCookies getCookies() {
		return this.cookies;
	}

	@Override
	public ServerInputStream getEntity() {
		return this.entityStream;
	}

}
