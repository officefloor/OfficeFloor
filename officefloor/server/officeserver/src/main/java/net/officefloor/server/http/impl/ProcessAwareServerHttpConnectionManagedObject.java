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
import java.io.Serializable;
import java.util.function.Supplier;

import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link ServerHttpConnection} implementation available for
 * {@link ExternalServiceInput}.
 * 
 * @param <B>
 *            Type of underlying buffer being used.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareServerHttpConnectionManagedObject<B>
		implements ServerHttpConnection, ProcessAwareManagedObject {

	/**
	 * Indicates if secure.
	 */
	private final boolean isSecure;

	/**
	 * {@link HttpRequest}.
	 */
	private HttpRequest request;

	/**
	 * {@link HttpRequest} entity {@link ByteSequence}.
	 */
	private ByteSequence requestEntity;

	/**
	 * {@link HttpResponse}.
	 */
	private ProcessAwareHttpResponse<B> response;

	/**
	 * {@link Supplier} for the {@link HttpMethod}.
	 */
	private final Supplier<HttpMethod> methodSupplier;

	/**
	 * {@link HttpRequestHeaders}.
	 */
	private final HttpRequestHeaders headers;

	/**
	 * {@link BufferPool}.
	 */
	private final BufferPool<B> bufferPool;

	/**
	 * {@link HttpResponseWriter}.
	 */
	private final HttpResponseWriter<B> httpResponseWriter;

	/**
	 * {@link HttpMethod}.
	 */
	private HttpMethod method = null;

	/**
	 * {@link ProcessAwareContext}.
	 */
	private ProcessAwareContext processAwareContext;

	/**
	 * Instantiate.
	 * 
	 * @param isSecure
	 *            Indicates if secure.
	 * @param methodSupplier
	 *            {@link Supplier} for the {@link HttpRequest}
	 *            {@link HttpMethod}.
	 * @param requestUriSupplier
	 *            {@link Supplier} for the {@link HttpRequest} URI.
	 * @param version
	 *            {@link HttpVersion} for the {@link HttpRequest}.
	 * @param requestHeaders
	 *            {@link NonMaterialisedHttpHeaders} for the
	 *            {@link HttpRequest}.
	 * @param requestEntity
	 *            {@link ByteSequence} for the {@link HttpRequest} entity.
	 * @param writer
	 *            {@link HttpResponseWriter}.
	 * @param bufferPool
	 *            {@link BufferPool}.
	 */
	public ProcessAwareServerHttpConnectionManagedObject(boolean isSecure, Supplier<HttpMethod> methodSupplier,
			Supplier<String> requestUriSupplier, HttpVersion version, NonMaterialisedHttpHeaders requestHeaders,
			ByteSequence requestEntity, HttpResponseWriter<B> writer, BufferPool<B> bufferPool) {

		// Indicate if secure
		this.isSecure = isSecure;

		// Create the HTTP request
		this.headers = new MaterialisingHttpRequestHeaders(requestHeaders);
		this.request = new MaterialisingHttpRequest(methodSupplier, requestUriSupplier, version, this.headers,
				requestEntity);
		this.methodSupplier = methodSupplier;
		this.requestEntity = requestEntity;

		// Store remaining state
		this.bufferPool = bufferPool;
		this.httpResponseWriter = writer;
	}

	/*
	 * =============== ProcessAwareContext ============================
	 */

	@Override
	public void setProcessAwareContext(ProcessAwareContext context) {
		this.processAwareContext = context;

		// Create the HTTP response (with context awareness)
		this.response = new ProcessAwareHttpResponse<B>(this.request.getHttpVersion(), this.bufferPool, context,
				this.httpResponseWriter);
	}

	@Override
	public Object getObject() {
		return this;
	}

	/*
	 * ================== ServerHttpConnection =======================
	 */

	@Override
	public boolean isSecure() {
		return this.isSecure;
	}

	@Override
	public HttpRequest getHttpRequest() {
		return this.request;
	}

	@Override
	public HttpResponse getHttpResponse() {
		return this.response;
	}

	@Override
	public Serializable exportState() throws IOException {
		return new SerialisableHttpRequest(this.request, this.requestEntity);
	}

	@Override
	public void importState(Serializable momento) throws IllegalArgumentException, IOException {
		this.processAwareContext.run(() -> {
			if (!(momento instanceof SerialisableHttpRequest)) {
				throw new IllegalArgumentException("Invalid momento to import state");
			}
			SerialisableHttpRequest serialisableRequest = (SerialisableHttpRequest) momento;
			this.request = serialisableRequest;
			this.requestEntity = serialisableRequest.getEntityByteSequence();
			return null;
		});
	}

	@Override
	public HttpMethod getClientHttpMethod() {
		if (this.method == null) {
			this.method = this.methodSupplier.get();
		}
		return this.method;
	}

	@Override
	public HttpRequestHeaders getClientHttpHeaders() {
		return this.headers;
	}

}