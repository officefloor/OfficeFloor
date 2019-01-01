package net.officefloor.server.http.servlet;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.servlet.AsyncContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.impl.HttpResponseWriter;
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

		// Start asynchronous servicing
		AsyncContext asyncContext = request.startAsync();

		// Obtain the request
		HttpServletRequest asyncRequest = (HttpServletRequest) asyncContext.getRequest();

		// Create the writer of the response
		HttpResponseWriter<ByteBuffer> writer = new HttpServletHttpResponseWriter(asyncContext, chain, bufferPool);

		// Create the request headers
		NonMaterialisedHttpHeaders httpHeaders = new HttpServletNonMaterialisedHttpHeaders(asyncRequest);

		// Create the entity content
		ByteSequence entity = new HttpServletEntityByteSequence(asyncRequest);

		// Create the server HTTP connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>(
				this.bridge.getHttpServerLocation(), request.isSecure(),
				() -> HttpMethod.getHttpMethod(asyncRequest.getMethod()),
				() -> asyncRequest.getRequestURI()
						+ (asyncRequest.getQueryString() != null ? "?" + asyncRequest.getQueryString() : ""),
				HttpVersion.getHttpVersion(request.getProtocol()), httpHeaders, entity, null, null,
				this.bridge.isIncludeEscalationStackTrace(), writer, bufferPool);

		// Service request
		this.bridge.getInput().service(connection, connection.getServiceFlowCallback());
	}

}