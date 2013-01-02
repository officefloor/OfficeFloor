/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.plugin.servlet.container.source;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.container.HttpServletContainer;
import net.officefloor.plugin.servlet.container.HttpServletServicer;
import net.officefloor.plugin.servlet.container.source.HttpServletTask.DependencyKeys;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.filter.FilterChainFactory;
import net.officefloor.plugin.servlet.mapping.MappingType;
import net.officefloor.plugin.servlet.mapping.ServicerMapping;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.stream.impl.MockServerOutputStream;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Tests the {@link HttpServletTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletTaskTest extends OfficeFrameTestCase {

	/**
	 * {@link ServletContext} path.
	 */
	private static final String CONTEXT_PATH = "/context/path";

	/**
	 * {@link HttpServlet} name.
	 */
	private static final String SERVLET_NAME = "ServletName";

	/**
	 * {@link Servlet} path.
	 */
	private static final String SERVLET_PATH = "/servlet/path";

	/**
	 * {@link HttpServlet} mappings.
	 */
	private static final String[] SERVLET_MAPPINGS = new String[] {
			SERVLET_PATH + "/*", "*.html", "*.htm" };

	/**
	 * {@link HttpServlet}.
	 */
	private final MockHttpServlet servlet = new MockHttpServlet();

	/**
	 * Initialisation parameters.
	 */
	private final Map<String, String> initParameters = new HashMap<String, String>();

	/**
	 * {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private final TaskContext<HttpServletTask, DependencyKeys, None> taskContext = this
			.createMock(TaskContext.class);

	/**
	 * {@link OfficeServletContext}.
	 */
	private final OfficeServletContext officeServletContext = this
			.createMock(OfficeServletContext.class);

	/**
	 * {@link MockServerOutputStream}.
	 */
	private final MockServerOutputStream entity = new MockServerOutputStream();

	/**
	 * {@link FilterChainFactory}.
	 */
	private final FilterChainFactory filterChainFactory = new FilterChainFactory() {
		@Override
		public FilterChain createFilterChain(ServicerMapping mapping,
				MappingType mappingType, FilterChain target)
				throws ServletException {
			return target; // no filtering
		}
	};

	/**
	 * {@link Office}.
	 */
	private final Office office = this.createMock(Office.class);

	/**
	 * {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * {@link HttpRequestState}.
	 */
	private final HttpRequestState attributes = this
			.createMock(HttpRequestState.class);

	/**
	 * {@link HttpSecurity}.
	 */
	private final HttpSecurity security = this.createMock(HttpSecurity.class);

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session = this.createMock(HttpSession.class);

	/**
	 * {@link ServicerMapping}.
	 */
	private ServicerMapping mapping = this.createMock(ServicerMapping.class);

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequest request = this.createMock(HttpRequest.class);

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponse response = this.createMock(HttpResponse.class);

	/**
	 * {@link HttpServletTask} to test.
	 */
	private HttpServletTask task;

	@Override
	protected void setUp() throws Exception {
		// Setup the work factory (and task factory)
		HttpServletTask factory = new HttpServletTask(SERVLET_NAME,
				this.servlet, this.initParameters, SERVLET_MAPPINGS);
		factory.setOffice(this.office);

		// Create the task
		HttpServletTask work = factory.createWork();
		this.task = (HttpServletTask) factory.createTask(work);
	}

	/**
	 * Ensure can service the {@link HttpRequest} with the {@link HttpServlet}.
	 */
	public void testService() throws Throwable {

		// Record obtain context for initialising Servlet
		this.record_service(true);

		// Test
		this.replayMockObjects();
		this.task.doTask(this.taskContext);
		this.verifyMockObjects();

		// Ensure service is invoked
		assertEquals("Should be initialised", 1, this.servlet.initCount);
		assertTrue("HttpServlet should service", this.servlet.isServiceInvoked);
	}

	/**
	 * Ensure can service the {@link HttpRequest} by re-using the
	 * {@link HttpServlet}.
	 */
	public void testServiceAgain() throws Throwable {

		// Record servicing twice (second time without init)
		this.record_service(true);
		this.record_service(false);

		// Test
		this.replayMockObjects();
		this.task.doTask(this.taskContext);
		this.task.doTask(this.taskContext);
		this.verifyMockObjects();

		// Ensure service is initialised only once in servicing
		assertEquals("Should only be initialised once", 1,
				this.servlet.initCount);
		assertTrue("HttpServlet should service", this.servlet.isServiceInvoked);
	}

	/**
	 * Ensure can have no {@link ServicerMapping}.
	 */
	public void testServiceNoMapping() throws Throwable {

		// No mapping
		this.mapping = null;

		// Record servicing (with no mapping)
		this.record_service(true);

		// Test
		this.replayMockObjects();
		this.task.doTask(this.taskContext);
		this.verifyMockObjects();

		// Ensure service is initialised only once in servicing
		assertEquals("Should only be initialised once", 1,
				this.servlet.initCount);
		assertTrue("HttpServlet should service", this.servlet.isServiceInvoked);
	}

	/**
	 * Ensure appropriately acts as a {@link HttpServletServicer}.
	 */
	public void testHttpServletDifferentiator() throws Exception {

		final HttpServletRequest request = this
				.createMock(HttpServletRequest.class);
		final HttpServletResponse response = this
				.createMock(HttpServletResponse.class);

		// Record servlet functionality
		this.recordReturn(this.officeServletContext,
				this.officeServletContext.getFilterChainFactory(this.office),
				this.filterChainFactory);
		this.recordReturn(request, request.getContextPath(), CONTEXT_PATH);
		this.recordReturn(request, request.getServletPath(), SERVLET_PATH);

		// Test
		this.replayMockObjects();

		// Utilise as differentiator
		HttpServletServicer differentiator = this.task;

		// Verify details
		assertEquals("Incorrect servlet name", SERVLET_NAME,
				differentiator.getServletName());
		String[] mappings = differentiator.getServletMappings();
		assertList(Arrays.asList(mappings), SERVLET_MAPPINGS);

		// Test include
		differentiator.include(this.officeServletContext, request, response);

		this.verifyMockObjects();

		// Ensure service is invoked
		assertTrue("HttpServlet should service", this.servlet.isServiceInvoked);
	}

	/**
	 * Records servicing the {@link HttpRequest}.
	 * 
	 * @param isCreateContainer
	 *            Flag indicating if expecting to create the
	 *            {@link HttpServletContainer}.
	 */
	private void record_service(boolean isCreateContainer) {
		try {

			// Record sourcing the dependencies for servicing the request
			this.recordReturn(this.taskContext, this.taskContext
					.getObject(DependencyKeys.OFFICE_SERVLET_CONTEXT),
					this.officeServletContext);
			this.recordReturn(this.taskContext,
					this.taskContext.getObject(DependencyKeys.HTTP_CONNECTION),
					this.connection);
			this.recordReturn(this.taskContext, this.taskContext
					.getObject(DependencyKeys.REQUEST_ATTRIBUTES),
					this.attributes);
			this.recordReturn(this.taskContext,
					this.taskContext.getObject(DependencyKeys.HTTP_SESSION),
					this.session);
			this.recordReturn(this.taskContext,
					this.taskContext.getObject(DependencyKeys.HTTP_SECURITY),
					this.security);
			this.recordReturn(
					this.taskContext,
					this.taskContext.getObject(DependencyKeys.SERVICER_MAPPING),
					this.mapping);

			if (isCreateContainer) {
				// Record obtaining filter chain factory in container creation
				this.recordReturn(this.officeServletContext,
						this.officeServletContext
								.getFilterChainFactory(this.office),
						this.filterChainFactory);
			}

			// Record last access time
			this.attributes.getAttribute("#HttpServlet.LastAccessTime#");
			this.control(this.attributes).setReturnValue(new Long(10));

			// Record obtaining the request and responses for servicing
			this.recordReturn(this.session, this.session.getTokenName(),
					"JSESSION_ID");
			this.recordReturn(this.connection,
					this.connection.getHttpRequest(), this.request);
			this.recordReturn(this.connection,
					this.connection.getHttpResponse(), this.response);
			this.recordReturn(this.request, this.request.getRequestURI(),
					CONTEXT_PATH + SERVLET_PATH);
			this.recordReturn(this.request, this.request.getMethod(), "GET");
			this.recordReturn(this.officeServletContext,
					this.officeServletContext.getContextPath(this.office),
					CONTEXT_PATH);
			this.recordReturn(this.response, this.response.getEntity(),
					this.entity.getServerOutputStream());

			// Record obtain context and servlet path in HTTP Servlet
			this.recordReturn(this.officeServletContext,
					this.officeServletContext.getContextPath(this.office),
					CONTEXT_PATH);
			if (this.mapping != null) {
				this.recordReturn(this.mapping, this.mapping.getServletPath(),
						SERVLET_PATH);
			}

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Mock {@link HttpServlet} for testing.
	 */
	private class MockHttpServlet extends HttpServlet {

		/**
		 * Indicates if the <code>service</code> method is invoke.
		 */
		public boolean isServiceInvoked = false;

		/**
		 * Indicates the number of time initialised.
		 */
		public int initCount = 0;

		/*
		 * ================= HttpServlet =========================
		 */

		@Override
		public void init() throws ServletException {
			this.initCount++;
		}

		@Override
		protected void service(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {

			// Ensure correct context path
			assertEquals("Incorrect context path", CONTEXT_PATH,
					req.getContextPath());

			// Ensure correct servlet path
			assertEquals("Incorrect Servlet path", SERVLET_PATH,
					req.getServletPath());

			// Flag invoked
			this.isServiceInvoked = true;
		}
	}

}