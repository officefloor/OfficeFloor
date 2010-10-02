/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.container.source.HttpServletTask.DependencyKeys;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.security.HttpSecurity;
import net.officefloor.plugin.socket.server.http.session.HttpSession;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * Tests the {@link HttpServletWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpServletWorkSource.class,
				HttpServletWorkSource.PROPERTY_SERVLET_NAME, "Servlet Name",
				HttpServletWorkSource.PROPERTY_SERVLET_PATH, "Servlet Path",
				HttpServletWorkSource.PROPERTY_HTTP_SERVLET_CLASS_NAME,
				"Servlet Class");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the work
		HttpServletTask factory = new HttpServletTask("ServletName",
				"/servlet/path", new MockHttpServlet(),
				new HashMap<String, String>());

		// Create expected type
		WorkTypeBuilder<HttpServletTask> type = WorkLoaderUtil
				.createWorkTypeBuilder(factory);
		TaskTypeBuilder<DependencyKeys, None> task = type.addTaskType(
				"service", factory, DependencyKeys.class, None.class);
		task.setDifferentiator(factory);
		task.addObject(OfficeServletContext.class).setKey(
				DependencyKeys.OFFICE_SERVLET_CONTEXT);
		task.addObject(ServerHttpConnection.class).setKey(
				DependencyKeys.HTTP_CONNECTION);
		task.addObject(Map.class).setKey(DependencyKeys.REQUEST_ATTRIBUTES);
		task.addObject(HttpSession.class).setKey(DependencyKeys.HTTP_SESSION);
		task.addObject(HttpSecurity.class).setKey(DependencyKeys.HTTP_SECURITY);
		task.addEscalation(ServletException.class);
		task.addEscalation(IOException.class);

		// Validate type
		WorkLoaderUtil.validateWorkType(type, HttpServletWorkSource.class,
				HttpServletWorkSource.PROPERTY_SERVLET_NAME, "ServletName",
				HttpServletWorkSource.PROPERTY_SERVLET_PATH, "/servlet/path",
				HttpServletWorkSource.PROPERTY_HTTP_SERVLET_CLASS_NAME,
				MockHttpServlet.class.getName());
	}

	/**
	 * Ensure able to service {@link HttpRequest}.
	 */
	@SuppressWarnings("unchecked")
	public void testService() throws Throwable {

		// Mocks
		final TaskContext<HttpServletTask, DependencyKeys, None> taskContext = this
				.createMock(TaskContext.class);
		final OfficeServletContext officeServletContext = this
				.createMock(OfficeServletContext.class);
		final ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		final Map<String, Object> attributes = this.createMock(Map.class);
		final HttpSession session = this.createMock(HttpSession.class);
		final HttpSecurity security = this.createMock(HttpSecurity.class);
		final HttpRequest request = this.createMock(HttpRequest.class);
		final HttpResponse response = this.createMock(HttpResponse.class);
		final OutputBufferStream body = this
				.createMock(OutputBufferStream.class);

		// Rest Mock HTTP Servlet for testing
		MockHttpServlet.reset();

		// Record servicing request
		this.recordReturn(taskContext, taskContext
				.getObject(DependencyKeys.OFFICE_SERVLET_CONTEXT),
				officeServletContext);
		this.recordReturn(taskContext, taskContext
				.getObject(DependencyKeys.HTTP_CONNECTION), connection);
		this.recordReturn(taskContext, taskContext
				.getObject(DependencyKeys.REQUEST_ATTRIBUTES), attributes);
		this.recordReturn(taskContext, taskContext
				.getObject(DependencyKeys.HTTP_SESSION), session);
		this.recordReturn(taskContext, taskContext
				.getObject(DependencyKeys.HTTP_SECURITY), security);
		attributes.get("#HttpServlet.LastAccessTime#");
		this.control(attributes).setReturnValue(new Long(1000));
		this.recordReturn(session, session.getTokenName(), "JSESSION_ID");
		this.recordReturn(connection, connection.getHttpRequest(), request);
		this.recordReturn(request, request.getRequestURI(),
				"http://www.officefloor.net");
		this.recordReturn(request, request.getMethod(), "GET");
		this.recordReturn(connection, connection.getHttpResponse(), response);
		this.recordReturn(response, response.getBody(), body);
		this.recordReturn(body, body.getOutputStream(),
				new ByteArrayOutputStream());
		this.recordReturn(request, request.getMethod(), "GET");

		// Test
		this.replayMockObjects();

		// Load the work type
		WorkType<HttpServletTask> type = WorkLoaderUtil.loadWorkType(
				HttpServletWorkSource.class,
				HttpServletWorkSource.PROPERTY_SERVLET_NAME, "ServletName",
				HttpServletWorkSource.PROPERTY_SERVLET_PATH, "/servlet/path",
				HttpServletWorkSource.PROPERTY_HTTP_SERVLET_CLASS_NAME,
				MockHttpServlet.class.getName(),
				HttpServletWorkSource.PROPERTY_PREFIX_INIT_PARAMETER + "test",
				"available");

		// Create the task and service request
		HttpServletTask task = type.getWorkFactory().createWork();
		task.doTask(taskContext);

		// Verify functionality
		this.verifyMockObjects();

		// Ensure appropriate methods invoked
		assertTrue("init() should be invoked", MockHttpServlet.isInitInvoked);
		assertTrue("doGet(...) should be invoked",
				MockHttpServlet.isDoGetInvoked);
	}

	/**
	 * Mock {@link HttpServlet} for testing.
	 */
	public static class MockHttpServlet extends HttpServlet {

		/**
		 * Flag indicating if {@link #init()} is invoked.
		 */
		public static boolean isInitInvoked = false;

		/**
		 * Flag indicating if
		 * {@link #doGet(HttpServletRequest, HttpServletResponse)} method
		 * invoked.
		 */
		public static boolean isDoGetInvoked = false;

		/**
		 * Reset for next test.
		 */
		public static void reset() {
			isInitInvoked = false;
			isDoGetInvoked = false;
		}

		/*
		 * ==================== HttpServlet ======================
		 */

		@Override
		public void init() throws ServletException {
			// Ensure appropriate configuration
			assertEquals("Incorrect servlet name", "ServletName", this
					.getServletName());
			assertEquals("Expecting init parameter", "available", this
					.getServletConfig().getInitParameter("test"));

			// Flag invoked
			isInitInvoked = true;
		}

		@Override
		protected void doGet(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			// Flag invoked
			isDoGetInvoked = true;
		}
	}

}