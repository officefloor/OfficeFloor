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

import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link ServerHttpConnection} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerHttpConnectionImpl implements ServerHttpConnection, ProcessAwareManagedObject {

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
	private final SerialisableHttpResponse response;

	/**
	 * {@link Supplier} for the {@link HttpMethod}.
	 */
	private final Supplier<HttpMethod> methodSupplier;

	/**
	 * {@link HttpMethod}.
	 */
	private HttpMethod method = null;

	/**
	 * {@link ProcessAwareContext}.
	 */
	private ProcessAwareContext processAwareContext;

	public ServerHttpConnectionImpl(boolean isSecure, Supplier<HttpMethod> methodSupplier,
			Supplier<String> requestUriSupplier, HttpVersion version, NonMaterialisedHttpHeaders requestHeaders,
			ByteSequence requestEntity, HttpResponseWriter writer, BufferPool byteBufferPool) {

		// Indicate if secure
		this.isSecure = isSecure;

		// Create the HTTP request
		this.request = new MaterialisingHttpRequest(methodSupplier, requestUriSupplier, version,
				new MaterialisingHttpRequestHeaders(requestHeaders), requestEntity);
		this.methodSupplier = methodSupplier;
		this.requestEntity = requestEntity;

		// Create the HTTP response
		this.response = new SerialisableHttpResponse(version);
	}

	/*
	 * =============== ProcessAwareContext ============================
	 */

	@Override
	public void setProcessAwareContext(ProcessAwareContext context) {
		this.processAwareContext = context;
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
			SerialisableHttpRequest serialisableRequest = (SerialisableHttpRequest) momento;
			this.request = serialisableRequest;
			this.requestEntity = serialisableRequest.getEntityByteSequence();
			return null;
		});
	}

	@Override
	public HttpMethod getHttpMethod() {
		if (this.method == null) {
			this.method = this.methodSupplier.get();
		}
		return this.method;
	}

}