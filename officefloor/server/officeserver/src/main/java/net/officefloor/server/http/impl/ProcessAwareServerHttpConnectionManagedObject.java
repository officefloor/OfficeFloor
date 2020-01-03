/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.http.impl;

import java.io.IOException;
import java.io.Serializable;
import java.util.function.Supplier;

import net.officefloor.compile.spi.officefloor.ExternalServiceCleanupEscalationHandler;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.server.http.DateHttpHeaderClock;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestCookies;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link ServerHttpConnection} implementation available for
 * {@link ExternalServiceInput}.
 * 
 * @param <B> Type of underlying buffer being used.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessAwareServerHttpConnectionManagedObject<B>
		implements ServerHttpConnection, ContextAwareManagedObject, FlowCallback {

	/**
	 * Obtains the {@link ExternalServiceCleanupEscalationHandler}.
	 * 
	 * @return {@link ExternalServiceCleanupEscalationHandler}.
	 */
	@SuppressWarnings("rawtypes")
	public static ExternalServiceCleanupEscalationHandler<ProcessAwareServerHttpConnectionManagedObject> getCleanupEscalationHandler() {
		return (inputManagedObject, cleanupEscalations) -> {
			inputManagedObject.setCleanupEscalations(cleanupEscalations);
		};
	}

	/**
	 * {@link HttpServerLocation}.
	 */
	private final HttpServerLocation serverLocation;

	/**
	 * Indicates if secure.
	 */
	private final boolean isSecure;

	/**
	 * {@link HttpRequest}.
	 */
	private HttpRequest request;

	/**
	 * Client {@link HttpRequest}.
	 */
	private final HttpRequest clientRequest;

	/**
	 * {@link HttpRequest} entity {@link ByteSequence}.
	 */
	private ByteSequence requestEntity;

	/**
	 * {@link HttpResponse}.
	 */
	private ProcessAwareHttpResponse<B> response;

	/**
	 * Name of the server. May be <code>null</code> to not send the
	 * <code>Server</code> {@link HttpHeader}.
	 */
	final HttpHeaderValue serverName;

	/**
	 * {@link DateHttpHeaderClock} to send the <code>Date</code> {@link HttpHeader}.
	 * May be <code>null</code> to not send.
	 */
	final DateHttpHeaderClock dateHttpHeaderClock;

	/**
	 * {@link StreamBufferPool}.
	 */
	final StreamBufferPool<B> bufferPool;

	/**
	 * Indicates whether to include the stack trace in {@link Escalation}
	 * {@link HttpResponse}.
	 */
	final boolean isIncludeStackTraceOnEscalation;

	/**
	 * {@link HttpResponseWriter}.
	 */
	final HttpResponseWriter<B> httpResponseWriter;

	/**
	 * {@link ManagedObjectContext}.
	 */
	private ManagedObjectContext managedObjectContext;

	/**
	 * Instantiate.
	 * 
	 * @param serverLocation                  {@link HttpServerLocation}.
	 * @param isSecure                        Indicates if secure.
	 * @param methodSupplier                  {@link Supplier} for the
	 *                                        {@link HttpRequest}
	 *                                        {@link HttpMethod}.
	 * @param requestUriSupplier              {@link Supplier} for the
	 *                                        {@link HttpRequest} URI.
	 * @param version                         {@link HttpVersion} for the
	 *                                        {@link HttpRequest}.
	 * @param requestHeaders                  {@link NonMaterialisedHttpHeaders} for
	 *                                        the {@link HttpRequest}.
	 * @param requestEntity                   {@link ByteSequence} for the
	 *                                        {@link HttpRequest} entity.
	 * @param serverName                      Name of the server. May be
	 *                                        <code>null</code> if not sending
	 *                                        <code>Server</code>
	 *                                        {@link HttpHeader}.
	 * @param dateHttpHeaderClock             {@link DateHttpHeaderClock}. May be
	 *                                        <code>null</code> to not send
	 *                                        <code>Date</code> {@link HttpHeader}.
	 * @param isIncludeStackTraceOnEscalation <code>true</code> to include the
	 *                                        {@link Escalation} stack trace in the
	 *                                        {@link HttpResponse}.
	 * @param writer                          {@link HttpResponseWriter}.
	 * @param bufferPool                      {@link StreamBufferPool}.
	 */
	public ProcessAwareServerHttpConnectionManagedObject(HttpServerLocation serverLocation, boolean isSecure,
			Supplier<HttpMethod> methodSupplier, Supplier<String> requestUriSupplier, HttpVersion version,
			NonMaterialisedHttpHeaders requestHeaders, ByteSequence requestEntity, HttpHeaderValue serverName,
			DateHttpHeaderClock dateHttpHeaderClock, boolean isIncludeStackTraceOnEscalation,
			HttpResponseWriter<B> writer, StreamBufferPool<B> bufferPool) {
		this.serverLocation = serverLocation;

		// Indicate if secure
		this.isSecure = isSecure;

		// Create the HTTP request
		HttpRequestHeaders headers = new MaterialisingHttpRequestHeaders(requestHeaders);
		HttpRequestCookies cookies = new MaterialisingHttpRequestCookies(headers);
		this.request = new MaterialisingHttpRequest(methodSupplier, requestUriSupplier, version, headers, cookies,
				requestEntity);
		this.clientRequest = this.request;
		this.requestEntity = requestEntity;

		// Store remaining state
		this.serverName = serverName;
		this.dateHttpHeaderClock = dateHttpHeaderClock;
		this.isIncludeStackTraceOnEscalation = isIncludeStackTraceOnEscalation;
		this.httpResponseWriter = writer;
		this.bufferPool = bufferPool;
	}

	/**
	 * Obtains the service {@link FlowCallback}.
	 * 
	 * @return {@link FlowCallback} to use for servicing this.
	 */
	public FlowCallback getServiceFlowCallback() {
		return this;
	}

	/**
	 * Sets the {@link CleanupEscalation} instances.
	 * 
	 * @param cleanupEscalations {@link CleanupEscalation} instances.
	 * @throws IOException If fails to send the {@link CleanupEscalation} details in
	 *                     the {@link HttpResponse}.
	 */
	public void setCleanupEscalations(CleanupEscalation[] cleanupEscalations) throws IOException {
		this.response.setCleanupEscalations(cleanupEscalations);
	}

	/*
	 * =============== ContextAwareManagedObject =====================
	 */

	@Override
	public void setManagedObjectContext(ManagedObjectContext context) {
		this.managedObjectContext = context;

		// Create the HTTP response (with context awareness)
		this.response = new ProcessAwareHttpResponse<B>(this, this.request.getVersion(), context);
	}

	@Override
	public Object getObject() {
		return this;
	}

	/*
	 * ================== ServerHttpConnection =======================
	 */

	@Override
	public HttpServerLocation getServerLocation() {
		return this.serverLocation;
	}

	@Override
	public boolean isSecure() {
		return this.isSecure;
	}

	@Override
	public HttpRequest getRequest() {
		return this.managedObjectContext.run(() -> this.request);
	}

	@Override
	public HttpResponse getResponse() {
		return this.response;
	}

	@Override
	public Serializable exportState() throws IOException {
		return new SerialisableHttpRequest(this.request, this.request.getCookies(), this.requestEntity);
	}

	@Override
	public void importState(Serializable momento) throws IllegalArgumentException, IOException {
		this.managedObjectContext.run(() -> {
			if (!(momento instanceof SerialisableHttpRequest)) {
				throw new IllegalArgumentException("Invalid momento to import state");
			}
			SerialisableHttpRequest state = (SerialisableHttpRequest) momento;
			SerialisableHttpRequest serialisableRequest = state.createHttpRequest(this.request.getVersion(),
					this.clientRequest.getCookies());
			this.request = serialisableRequest;
			this.requestEntity = serialisableRequest.getEntityByteSequence();
			return null;
		});
	}

	@Override
	public HttpRequest getClientRequest() {
		return this.clientRequest;
	}

	/*
	 * ================ FlowCallback =======================
	 */

	@Override
	public void run(Throwable escalation) throws Throwable {
		this.response.flushResponseToHttpResponseWriter(escalation);
	}

}
