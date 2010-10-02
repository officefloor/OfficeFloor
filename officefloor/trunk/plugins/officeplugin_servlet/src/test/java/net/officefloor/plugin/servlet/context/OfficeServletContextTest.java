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
package net.officefloor.plugin.servlet.context;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.AbstractMatcher;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.container.HttpServletDifferentiator;
import net.officefloor.plugin.servlet.container.ServletRequestForwarder;
import net.officefloor.plugin.servlet.log.Logger;
import net.officefloor.plugin.servlet.resource.ResourceLocator;

/**
 * Tests the {@link OfficeServletContext}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeServletContextTest extends OfficeFrameTestCase {

	/**
	 * Server name.
	 */
	private final String serverName = "www.officefloor.net";

	/**
	 * Name of the {@link ServletContext}.
	 */
	private final String servletContextName = "ServletContextName";

	/**
	 * Context path.
	 */
	private final String contextPath = "/context/path";

	/**
	 * Init parameters.
	 */
	private final Map<String, String> initParameters = new HashMap<String, String>();

	/**
	 * Mapping of file extension to MIME type.
	 */
	private final Map<String, String> mimeMappings = new HashMap<String, String>();

	/**
	 * {@link ResourceLocator}.
	 */
	private final ResourceLocator locator = this
			.createMock(ResourceLocator.class);

	/**
	 * {@link Logger}.
	 */
	private final Logger logger = this.createMock(Logger.class);

	/**
	 * {@link Office}.
	 */
	private final Office office = this.createMock(Office.class);

	/**
	 * {@link OfficeServletContext} to test.
	 */
	private final OfficeServletContext context = new OfficeServletContextImpl(
			this.serverName, 80, this.servletContextName, this.contextPath,
			this.initParameters, this.mimeMappings, this.locator, this.logger);

	/**
	 * Ensure correct context path.
	 */
	public void testContextPath() {
		assertEquals("Incorrect context path", this.contextPath, this.context
				.getContextPath(this.office));
	}

	/**
	 * Due to security (and simplicity) can not obtain other
	 * {@link ServletContext} instances.
	 */
	public void testOtherContexts() {
		assertNull("Restricting access to other servlet contexts", this.context
				.getContext(this.office, "/other"));
	}

	/**
	 * Ensure correct MIME type for files.
	 */
	public void testFileMimeTypes() {
		this.mimeMappings.put("html", "text/html");
		assertNull("No extension so no MIME type", this.context.getMimeType(
				this.office, "NoExtensionFileName"));
		assertEquals("Incorrect MIME type", "text/html", this.context
				.getMimeType(this.office, "file.html"));
		assertNull("Unknown file extension", this.context.getMimeType(
				this.office, "file.unknown"));
	}

	/**
	 * Ensure can obtain resources.
	 */
	public void testResources() throws Exception {

		// Record obtaining resource paths
		final Set<String> children = new HashSet<String>(Arrays
				.asList("/child/"));
		this.recordReturn(this.locator, this.locator
				.getResourceChildren("/parent"), children);

		// Record obtain resource URL
		final URL url = new URL("http", "officefloor.net", 80, "resource");
		this.recordReturn(this.locator, this.locator.getResource("/resource"),
				url);

		// Record obtain resource stream
		final InputStream stream = new ByteArrayInputStream(new byte[] { 1 });
		this.recordReturn(this.locator, this.locator
				.getResourceAsStream("/resource"), stream);

		// Test
		this.replayMockObjects();
		assertEquals("Incorrect resource paths", children, this.context
				.getResourcePaths(this.office, "/parent"));
		assertEquals("Incorrect resource URL", url.toString(), this.context
				.getResource(this.office, "/resource").toString());
		assertEquals("Incorrect resource InputStream", stream, this.context
				.getResourceAsStream(this.office, "/resource"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can obtain {@link RequestDispatcher}.
	 */
	public void testRequestDispatcher() throws Exception {

		final WorkManager workManager = this.createMock(WorkManager.class);
		final TaskManager taskManager = this.createMock(TaskManager.class);
		final HttpServletDifferentiator differentiator = this
				.createMock(HttpServletDifferentiator.class);
		final HttpServletRequest request = this
				.createMock(HttpServletRequest.class);
		final HttpServletResponse response = this
				.createMock(HttpServletResponse.class);
		final ServletRequestForwarder forwarder = this
				.createMock(ServletRequestForwarder.class);
		final String WORK_NAME = "WORK";
		final String TASK_NAME = "TASK";

		// Record reflection on Office for RequestDispatcher
		this.recordReturn(this.office, this.office.getWorkNames(),
				new String[] { WORK_NAME });
		this.recordReturn(this.office, this.office.getWorkManager(WORK_NAME),
				workManager);
		this.recordReturn(workManager, workManager.getTaskNames(),
				new String[] { TASK_NAME });
		this.recordReturn(workManager, workManager.getTaskManager(TASK_NAME),
				taskManager);
		this.recordReturn(taskManager, taskManager.getDifferentiator(),
				differentiator);
		this.recordReturn(differentiator, differentiator.getServletPath(),
				"/resource");
		this
				.recordReturn(differentiator, differentiator.getServletName(),
						null);
		this.recordReturn(differentiator, differentiator.getExtensions(), null);

		// Record forwarding
		this.recordReturn(request, request
				.getAttribute(ServletRequestForwarder.ATTRIBUTE_FORWARDER),
				forwarder);
		forwarder.forward(WORK_NAME, TASK_NAME, null);

		// Record including
		differentiator.include(this.context, request, response);
		this.control(differentiator).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect context", expected[0], actual[0]);
				HttpServletRequest request = (HttpServletRequest) actual[1];
				assertEquals("Incorrect request", "/resource", request
						.getPathInfo());
				assertEquals("Incorrect response", expected[2], actual[2]);
				return true;
			}
		});

		// Record allowing illegal state of no access available
		this.recordReturn(request, request
				.getAttribute(ServletRequestForwarder.ATTRIBUTE_FORWARDER),
				null);

		// Test
		this.replayMockObjects();

		// Obtain the dispatcher
		RequestDispatcher dispatcher = this.context.getRequestDispatcher(
				this.office, "/resource");
		assertNotNull("Ensure have dispatcher", dispatcher);

		// Forward to the dispatcher
		dispatcher.forward(request, response);

		// Include content from the dispatcher
		dispatcher.include(request, response);

		// Attempt forward with illegal state of no access available
		try {
			dispatcher.forward(request, response);
		} catch (IllegalStateException ex) {
			assertEquals(
					"Incorrect cause",
					"ServletRequestForwarder must be available from the ServletRequest",
					ex.getMessage());
		}

		// Attempt to obtain unknown dispatcher
		assertNull("Should not obtain dispatcher", this.context
				.getRequestDispatcher(this.office, "/unknown"));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can obtain {@link RequestDispatcher}.
	 */
	public void testNamedDispatcher() throws Exception {

		final WorkManager workManager = this.createMock(WorkManager.class);
		final TaskManager taskManager = this.createMock(TaskManager.class);
		final HttpServletDifferentiator differentiator = this
				.createMock(HttpServletDifferentiator.class);
		final HttpServletRequest request = this
				.createMock(HttpServletRequest.class);
		final HttpServletResponse response = this
				.createMock(HttpServletResponse.class);
		final ServletRequestForwarder forwarder = this
				.createMock(ServletRequestForwarder.class);

		// Record reflection on Office for RequestDispatcher
		this.recordReturn(this.office, this.office.getWorkNames(),
				new String[] { "WORK" });
		this.recordReturn(this.office, this.office.getWorkManager("WORK"),
				workManager);
		this
				.recordReturn(workManager, workManager.getTaskNames(),
						new String[] { "HTTP_SERVLET", "NO_NAME", "NULL",
								"WRONG_TYPE" });

		// Record the HTTP Servlet to use
		this.recordReturn(workManager, workManager
				.getTaskManager("HTTP_SERVLET"), taskManager);
		this.recordReturn(taskManager, taskManager.getDifferentiator(),
				differentiator);
		this.recordReturn(differentiator, differentiator.getServletPath(),
				"/resource");
		this.recordReturn(differentiator, differentiator.getServletName(),
				"NAME");
		this.recordReturn(differentiator, differentiator.getExtensions(), null);

		// Record HTTP Servlet without a name
		this.recordReturn(workManager, workManager.getTaskManager("NO_NAME"),
				taskManager);
		this.recordReturn(taskManager, taskManager.getDifferentiator(),
				differentiator);
		this.recordReturn(differentiator, differentiator.getServletPath(),
				"/another");
		this
				.recordReturn(differentiator, differentiator.getServletName(),
						null);
		this.recordReturn(differentiator, differentiator.getExtensions(), null);

		// Record no differentiator
		this.recordReturn(workManager, workManager.getTaskManager("NULL"),
				taskManager);
		this.recordReturn(taskManager, taskManager.getDifferentiator(), null);

		// Record differentiator of wrong type
		this.recordReturn(workManager,
				workManager.getTaskManager("WRONG_TYPE"), taskManager);
		this.recordReturn(taskManager, taskManager.getDifferentiator(),
				new Object());

		// Record using Servlet Path for Named Dispatcher
		final String SERVLET_PATH = "/servlet/path";
		this.recordReturn(differentiator, differentiator.getServletPath(),
				SERVLET_PATH);

		// Record forwarding
		this.recordReturn(request, request
				.getAttribute(ServletRequestForwarder.ATTRIBUTE_FORWARDER),
				forwarder);
		forwarder.forward("WORK", "HTTP_SERVLET", null);

		// Record including
		differentiator.include(this.context, request, response);
		this.control(differentiator).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect context", expected[0], actual[0]);
				HttpServletRequest request = (HttpServletRequest) actual[1];
				assertEquals("Incorrect request", SERVLET_PATH, request
						.getPathInfo());
				assertEquals("Incorrect response", expected[2], actual[2]);
				return true;
			}
		});

		// Test
		this.replayMockObjects();

		// Obtain the dispatcher
		RequestDispatcher dispatcher = this.context.getNamedDispatcher(
				this.office, "NAME");
		assertNotNull("Ensure have dispatcher", dispatcher);

		// Forward to the dispatcher
		dispatcher.forward(request, response);

		// Include content from the dispatcher
		dispatcher.include(request, response);

		// Attempt to obtain unknown dispatcher
		assertNull("Should not obtain dispatcher", this.context
				.getNamedDispatcher(this.office, "UNKNOWN"));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can obtain {@link RequestDispatcher} by extension.
	 */
	public void testExtensionDispatcher() throws Exception {

		final WorkManager workManager = this.createMock(WorkManager.class);
		final TaskManager taskManager = this.createMock(TaskManager.class);
		final HttpServletDifferentiator differentiator = this
				.createMock(HttpServletDifferentiator.class);
		final HttpServletRequest request = this
				.createMock(HttpServletRequest.class);
		final HttpServletResponse response = this
				.createMock(HttpServletResponse.class);
		final ServletRequestForwarder forwarder = this
				.createMock(ServletRequestForwarder.class);
		final String WORK_NAME = "WORK";
		final String TASK_NAME = "TASK";

		// Record reflection on Office for RequestDispatcher
		this.recordReturn(this.office, this.office.getWorkNames(),
				new String[] { WORK_NAME });
		this.recordReturn(this.office, this.office.getWorkManager(WORK_NAME),
				workManager);
		this.recordReturn(workManager, workManager.getTaskNames(),
				new String[] { TASK_NAME });
		this.recordReturn(workManager, workManager.getTaskManager(TASK_NAME),
				taskManager);
		this.recordReturn(taskManager, taskManager.getDifferentiator(),
				differentiator);
		this.recordReturn(differentiator, differentiator.getServletPath(),
				"/resource");
		this
				.recordReturn(differentiator, differentiator.getServletName(),
						null);
		this.recordReturn(differentiator, differentiator.getExtensions(),
				new String[] { "JSP" });

		// Record forwarding
		this.recordReturn(request, request
				.getAttribute(ServletRequestForwarder.ATTRIBUTE_FORWARDER),
				forwarder);
		forwarder.forward(WORK_NAME, TASK_NAME, null);

		// Record including
		differentiator.include(this.context, request, response);
		this.control(differentiator).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect context", expected[0], actual[0]);
				HttpServletRequest request = (HttpServletRequest) actual[1];
				assertEquals("Incorrect request", "/extension.jsp", request
						.getPathInfo());
				assertEquals("Incorrect response", expected[2], actual[2]);
				return true;
			}
		});

		// Test
		this.replayMockObjects();

		// Obtain the dispatcher
		RequestDispatcher dispatcher = this.context.getRequestDispatcher(
				this.office, "/extension.jsp");
		assertNotNull("Ensure have dispatcher", dispatcher);

		// Forward to the dispatcher
		dispatcher.forward(request, response);

		// Include content from the dispatcher
		dispatcher.include(request, response);

		// Attempt to obtain unknown dispatcher
		assertNull("Should not obtain dispatcher", this.context
				.getRequestDispatcher(this.office, "/unknown.ukn"));

		this.verifyMockObjects();
	}

	/**
	 * Ensure can log.
	 */
	public void testLogging() {

		// Record logging
		final String message = "Log Message";
		final Throwable failure = new Throwable("test");
		this.logger.log(message);
		this.logger.log(message, failure);

		// Test
		this.replayMockObjects();
		this.context.log(this.office, message);
		this.context.log(this.office, message, failure);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain real path.
	 */
	public void testRealPath() {
		this.replayMockObjects();
		assertEquals("Incorrect real path", "http://" + this.serverName
				+ this.contextPath + "/real.path", this.context.getRealPath(
				this.office, "/real.path"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure correct server info.
	 */
	public void testServerInfo() {
		this.replayMockObjects();
		String serverInfo = "OfficeFloor servlet plug-in/1.0";
		System.out.println("Validating server info to be '" + serverInfo + "'");
		assertEquals("Incorrect server info", serverInfo, this.context
				.getServerInfo(this.office));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can obtain init parameters.
	 */
	public void testInitParameters() {
		// Add initial parameter for use
		this.initParameters.put("name", "value");

		// Test
		this.replayMockObjects();
		assertEquals("Incorrect init parameter value", "value", this.context
				.getInitParameter(this.office, "name"));
		Enumeration<?> enumeration = this.context
				.getInitParameterNames(this.office);
		assertTrue("Expecting init parameter name", enumeration
				.hasMoreElements());
		assertEquals("Incorrect init parameter name", "name", enumeration
				.nextElement());
		assertFalse("Expecting only one init parameter name", enumeration
				.hasMoreElements());
		this.verifyMockObjects();
	}

	/**
	 * Ensure can set, obtain and remove attributes for a particular
	 * {@link Office}.
	 */
	@SuppressWarnings("unchecked")
	public void testAttributes() {
		final Object attribute = new Object();

		// Record loading office context (none for simplicity)
		this.recordReturn(this.office, this.office.getWorkNames(),
				new String[0]);

		// Test
		this.replayMockObjects();
		this.context.setAttribute(this.office, "attribute", attribute);
		assertEquals("Incorrect attribute", attribute, this.context
				.getAttribute(this.office, "attribute"));
		Enumeration<String> enumeration = this.context
				.getAttributeNames(this.office);
		assertTrue("Expecting an attribute name", enumeration.hasMoreElements());
		assertEquals("Incorrect attribute name", "attribute", enumeration
				.nextElement());
		assertFalse("Expecting only one attribute name", enumeration
				.hasMoreElements());
		this.context.removeAttribute(this.office, "attribute");
		assertNull("Attribute should be removed", this.context.getAttribute(
				this.office, "attribute"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure attributes are isolated to a particular {@link Office}.
	 */
	public void testAttributeIsolation() {

		final Office one = this.createMock(Office.class);
		final Office two = this.createMock(Office.class);
		final String NAME = "ATTRIBUTE";
		final Object VALUE = "VALUE";

		// Record loading office context (none for simplicity)
		this.recordReturn(one, one.getWorkNames(), new String[0]);
		this.recordReturn(two, two.getWorkNames(), new String[0]);

		// Test
		this.replayMockObjects();

		// Add to an Office is not visible in another Office
		this.context.setAttribute(one, NAME, VALUE);
		assertEquals("Incorrect attribute", VALUE, this.context.getAttribute(
				one, NAME));
		assertNull("Attribute should not be available from another Office",
				this.context.getAttribute(two, NAME));

		// Remove from an Office does not affect another Office
		this.context.removeAttribute(two, NAME);
		assertEquals("Should still be available", VALUE, this.context
				.getAttribute(one, NAME));

		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain servlet context name.
	 */
	public void testServletContextName() {
		assertEquals("Incorrect servlet context name", this.servletContextName,
				this.context.getServletContextName(this.office));
	}

}