/*-
 * #%L
 * HttpServlet adapter for OfficeFloor HTTP Server
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

package net.officefloor.server.http.servlet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 * {@link OfficeFloor} {@link HttpFilter}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorFilter implements Filter {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(OfficeFloorFilter.class.getName());

	/**
	 * {@link StreamBufferPool}.
	 */
	private static final StreamBufferPool<ByteBuffer> bufferPool = new ThreadLocalStreamBufferPool(
			() -> ByteBuffer.allocate(1024), 10, 1000);

	/**
	 * {@link FilterConfig}.
	 */
	private FilterConfig filterConfig;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * <p>
	 * {@link HttpServletOfficeFloorBridge}.
	 * <p>
	 * Lazy initialisation of {@link Filter} could cause different {@link Thread} to
	 * create bridge and subsequently consider it <code>null</code>.
	 */
	private volatile HttpServletOfficeFloorBridge bridge;

	/*
	 * =============== Filter =========================
	 */

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;

		// Indicate starting
		LOGGER.info("Initialising " + this.getClass().getSimpleName());

		// Open OfficeFloor (capturing bridge)
		try {
			this.bridge = HttpServletHttpServerImplementation.load(() -> {
				OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
				this.officeFloor = compiler.compile("OfficeFloor");
				this.officeFloor.openOfficeFloor();
			});
		} catch (Throwable ex) {
			// Indicate failure
			LOGGER.log(Level.SEVERE, "Failed to initialise " + this.getClass().getSimpleName(), ex);

			// Propagate the failure
			throw new ServletException(ex);
		}

		// Indicate started
		LOGGER.info(this.getClass().getSimpleName() + " initialised");
	}

	@Override
	public void destroy() {

		// Indicate destroying
		LOGGER.info("Destroying " + this.getClass().getSimpleName());

		// Close OfficeFloor
		if (this.officeFloor != null) {
			try {
				this.officeFloor.close();
			} catch (Exception ex) {
				this.filterConfig.getServletContext().log("Failed to close OfficeFloor", ex);
			}
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		// Obtain the HTTP request and response
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		// Create the request headers
		NonMaterialisedHttpHeaders httpHeaders = new HttpServletNonMaterialisedHttpHeaders(httpRequest);

		// Create the entity content
		ByteSequence entity;
		synchronized (httpRequest) {
			entity = new HttpServletEntityByteSequence(httpRequest);
		}

		// Create the writer of the response
		HttpServletHttpResponseWriter writer;
		synchronized (httpResponse) {
			writer = new HttpServletHttpResponseWriter(httpResponse, bufferPool);
		}

		// Create the server HTTP connection
		HttpMethod httpMethod = HttpMethod.getHttpMethod(httpRequest.getMethod());
		String requestUri = httpRequest.getRequestURI()
				+ (httpRequest.getQueryString() != null ? "?" + httpRequest.getQueryString() : "");
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>(
				this.bridge.getHttpServerLocation(), httpRequest.isSecure(), () -> httpMethod, () -> requestUri,
				HttpVersion.getHttpVersion(request.getProtocol()), httpHeaders, entity, null, null,
				this.bridge.isIncludeEscalationStackTrace(), writer, bufferPool);

		// Service request (blocks until serviced)
		this.bridge.getInput().service(connection, connection.getServiceFlowCallback());

		// Determine if serviced
		if (!writer.isServiced()) {
			// Not serviced, so allow servlet container to service
			chain.doFilter(httpRequest, httpResponse);
		}
	}

}
