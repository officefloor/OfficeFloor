/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.server.stress;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.DateHttpHeaderClock;
import net.officefloor.server.http.impl.HttpResponseWriter;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteArrayByteSequence;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link OfficeFloor} {@link HttpServlet}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorHttpServlet extends HttpServlet {

	/**
	 * {@link ExternalServiceInput} to service the {@link HttpServletRequest}
	 * instances.
	 */
	@SuppressWarnings("rawtypes")
	private final ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> input;

	/**
	 * {@link HttpServerLocation}.
	 */
	private final HttpServerLocation serverLocation;

	/**
	 * Name of the server.
	 */
	private final HttpHeaderValue serverName;

	/**
	 * {@link DateHttpHeaderClock}.
	 */
	private final DateHttpHeaderClock dateHttpHeaderClock;

	/**
	 * Indicates if include stack trace.
	 */
	private final boolean isIncludeEscalationStackTrace;

	/**
	 * {@link StreamBufferPool}.
	 */
	private final StreamBufferPool<ByteBuffer> bufferPool;

	/**
	 * Instantiate.
	 * 
	 * @param input                         {@link ExternalServiceInput} to service
	 *                                      the {@link HttpServletRequest}
	 *                                      instances.
	 * @param serverLocation                {@link HttpServerLocation}.
	 * @param serverName                    Name of the server.
	 * @param dateHttpHeaderClock           {@link DateHttpHeaderClock}.
	 * @param isIncludeEscalationStackTrace Indicates if include stack trace.
	 * @param bufferPool                    {@link StreamBufferPool}.
	 */
	@SuppressWarnings("rawtypes")
	public OfficeFloorHttpServlet(
			ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject> input,
			HttpServerLocation serverLocation, HttpHeaderValue serverName, DateHttpHeaderClock dateHttpHeaderClock,
			boolean isIncludeEscalationStackTrace, StreamBufferPool<ByteBuffer> bufferPool) {
		this.input = input;
		this.serverLocation = serverLocation;
		this.serverName = serverName;
		this.dateHttpHeaderClock = dateHttpHeaderClock;
		this.isIncludeEscalationStackTrace = isIncludeEscalationStackTrace;
		this.bufferPool = bufferPool;
	}

	/*
	 * ================== HttpServlet =========================
	 */

	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		// Start asynchronous servicing
		AsyncContext asyncContext = request.startAsync();

		// Obtain the request / response
		HttpServletRequest asyncRequest = (HttpServletRequest) asyncContext.getRequest();

		// Create the writer of the response
		HttpResponseWriter<ByteBuffer> writer = new HttpServletHttpResponseWriter(asyncContext, (asyncResponse) -> {
			// Load constant headers
			asyncResponse.setHeader("Date", "NOW");
			asyncResponse.setHeader("Server", this.serverName.getValue());
			asyncResponse.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
		}, this.bufferPool);

		// Create the request headers
		NonMaterialisedHttpHeaders httpHeaders = new HttpServletNonMaterialisedHttpHeaders(asyncRequest);

		// Create the entity content
		ByteSequence entity = new ByteArrayByteSequence(asyncRequest.getInputStream().readAllBytes());

		// Create the server HTTP connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>(
				this.serverLocation, request.isSecure(), () -> HttpMethod.getHttpMethod(request.getMethod()),
				() -> asyncRequest.getRequestURI()
						+ (asyncRequest.getQueryString() != null ? "?" + asyncRequest.getQueryString() : ""),
				HttpVersion.getHttpVersion(request.getProtocol()), httpHeaders, entity, this.serverName,
				this.dateHttpHeaderClock, this.isIncludeEscalationStackTrace, writer, this.bufferPool);

		// Service request
		this.input.service(connection, connection.getServiceFlowCallback());
	}

}