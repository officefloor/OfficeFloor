package net.officefloor.server.http.servlet;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	 * {@link HttpServletOfficeFloorBridge}.
	 */
	private HttpServletOfficeFloorBridge bridge;

	/*
	 * =============== Filter =========================
	 */

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;

		// Open OfficeFloor (capturing bridge)
		try {
			this.bridge = HttpServletHttpServerImplementation.load(() -> {
				OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
				this.officeFloor = compiler.compile("OfficeFloor");
				this.officeFloor.openOfficeFloor();
			});
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
	}

	@Override
	public void destroy() {

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
		ByteSequence entity = new HttpServletEntityByteSequence(httpRequest);

		// Create the writer of the response
		HttpServletHttpResponseWriter writer = new HttpServletHttpResponseWriter(httpResponse, bufferPool);

		// Create the server HTTP connection
		HttpMethod httpMethod = HttpMethod.getHttpMethod(httpRequest.getMethod());
		String requestUri = httpRequest.getRequestURI()
				+ (httpRequest.getQueryString() != null ? "?" + httpRequest.getQueryString() : "");
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>(
				this.bridge.getHttpServerLocation(), httpRequest.isSecure(), () -> httpMethod, () -> requestUri,
				HttpVersion.getHttpVersion(request.getProtocol()), httpHeaders, entity, null, null,
				this.bridge.isIncludeEscalationStackTrace(), writer, bufferPool);

		// Ensure state synchronized (before servicing by potentially other threads)
		synchronized (httpRequest) {
		}
		synchronized (httpResponse) {
		}

		// Service request (blocks until serviced)
		this.bridge.getInput().service(connection, connection.getServiceFlowCallback());

		// Determine if serviced
		if (!writer.isServiced()) {
			// Not serviced, so allow servlet container to service
			chain.doFilter(httpRequest, httpResponse);
		}
	}

}