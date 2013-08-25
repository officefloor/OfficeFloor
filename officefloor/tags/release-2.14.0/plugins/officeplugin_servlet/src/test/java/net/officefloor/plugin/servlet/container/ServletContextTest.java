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
package net.officefloor.plugin.servlet.container;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.context.OfficeServletContext;

/**
 * Tests the {@link ServletContextImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletContextTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeServletContext}.
	 */
	private final OfficeServletContext context = this
			.createMock(OfficeServletContext.class);

	/**
	 * {@link Office}.
	 */
	private final Office office = this.createMock(Office.class);

	/**
	 * {@link ServletContextImpl} to test.
	 */
	private final ServletContext servletContext = new ServletContextImpl(
			this.context, this.office);

	/**
	 * Ensure correct context path.
	 */
	public void testContextPath() {

		// Record obtaining Context Path
		final String CONTEXT_PATH = "/context/path";
		this.recordReturn(this.context, this.context
				.getContextPath(this.office), CONTEXT_PATH);

		// Test
		this.replayMockObjects();
		assertEquals("Incorrect context path", CONTEXT_PATH,
				this.servletContext.getContextPath());
		this.verifyMockObjects();
	}

	/**
	 * Obtain other {@link ServletContext} instances.
	 */
	public void testOtherContexts() {

		// Record obtaining available ServletContext
		final String AVAILABLE_URL = "/available";
		final ServletContext CONTEXT = this.createMock(ServletContext.class);
		this.recordReturn(this.context, this.context.getContext(this.office,
				AVAILABLE_URL), CONTEXT);

		// Record unavailable
		final String UNAVAILABLE_URL = "/unavailable";
		this.recordReturn(this.context, this.context.getContext(this.office,
				UNAVAILABLE_URL), null);

		// Test
		this.replayMockObjects();
		assertEquals("Incorrect ServletContext", CONTEXT, this.servletContext
				.getContext(AVAILABLE_URL));
		assertNull("Should be unavailable", this.servletContext
				.getContext(UNAVAILABLE_URL));
		this.verifyMockObjects();
	}

	/**
	 * Ensure correct version supported.
	 */
	public void testVersion() {

		// Versions
		final int MAJOR = 2;
		final int MINOR = 5;

		// Test
		this.replayMockObjects();
		assertEquals("Incorrect major version", MAJOR, this.servletContext
				.getMajorVersion());
		assertEquals("Incorrect minor version", MINOR, this.servletContext
				.getMinorVersion());
		this.verifyMockObjects();
	}

	/**
	 * Ensure correct MIME type for files.
	 */
	public void testFileMimeTypes() {

		// Record obtain MIME type
		final String FILE = "file.html";
		final String MIME_TYPE = "text/html";
		this.recordReturn(this.context, this.context.getMimeType(this.office,
				FILE), MIME_TYPE);

		// Test
		this.replayMockObjects();
		assertEquals("Incorrect MIME type", MIME_TYPE, this.servletContext
				.getMimeType(FILE));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can obtain resources.
	 */
	@SuppressWarnings("unchecked")
	public void testResources() throws Exception {

		// Record obtaining resource paths
		final Set<String> children = this.createMock(Set.class);
		this.recordReturn(this.context, this.context.getResourcePaths(
				this.office, "/parent"), children);

		// Record obtain resource URL
		final URL url = new URL("http", "officefloor.net", 80, "resource");
		this.recordReturn(this.context, this.context.getResource(this.office,
				"/resource"), url);

		// Record obtain resource stream
		final InputStream stream = new ByteArrayInputStream(new byte[] { 1 });
		this.recordReturn(this.context, this.context.getResourceAsStream(
				this.office, "/resource"), stream);

		// Test
		this.replayMockObjects();
		assertEquals("Incorrect resource paths", children, this.servletContext
				.getResourcePaths("/parent"));
		assertEquals("Incorrect resource URL", url.toString(),
				this.servletContext.getResource("/resource").toString());
		assertEquals("Incorrect resource InputStream", stream,
				this.servletContext.getResourceAsStream("/resource"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can create {@link RequestDispatcher}.
	 */
	public void testRequestDispatcher() {
		final RequestDispatcher dispatcher = this
				.createMock(RequestDispatcher.class);

		// Record obtaining Request Dispatcher
		this.recordReturn(this.context, this.context.getRequestDispatcher(
				this.office, "/resource"), dispatcher);

		// Record obtaining Named Dispatcher
		this.recordReturn(this.context, this.context.getNamedDispatcher(
				this.office, "Name"), dispatcher);

		// Test
		this.replayMockObjects();
		assertEquals("Incorrect request dispatcher", dispatcher,
				this.servletContext.getRequestDispatcher("/resource"));
		assertEquals("Incorrect named dispatcher", dispatcher,
				this.servletContext.getNamedDispatcher("Name"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can log.
	 */
	public void testLogging() {
		final String message = "Log Message";
		final Throwable failure = new Throwable("test");

		// Recording various logging
		this.context.log(this.office, message);
		this.context.log(this.office, message, failure);

		// Test
		this.replayMockObjects();
		this.servletContext.log(message);
		this.servletContext.log(message, failure);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain real path.
	 */
	public void testRealPath() {

		// Record obtaining real path
		final String RELATIVE_PATH = "/relative.path";
		final String REAL_PATH = "http://www.officefloor.net" + RELATIVE_PATH;
		this.recordReturn(this.context, this.context.getRealPath(this.office,
				RELATIVE_PATH), REAL_PATH);

		// Test
		this.replayMockObjects();
		assertEquals("Incorrect real path", REAL_PATH, this.servletContext
				.getRealPath(RELATIVE_PATH));
		this.verifyMockObjects();
	}

	/**
	 * Ensure correct server information.
	 */
	public void testServerInfo() {

		// Record obtaining Server Info
		final String SERVER_INFO = "Server Info";
		this.recordReturn(this.context,
				this.context.getServerInfo(this.office), SERVER_INFO);

		// Test
		this.replayMockObjects();
		assertEquals("Incorrect server info", SERVER_INFO, this.servletContext
				.getServerInfo());
		this.verifyMockObjects();
	}

	/**
	 * Ensure can obtain init parameters.
	 */
	public void testInitParameters() {

		// Record obtain init parameter
		final String NAME = "name";
		final String VALUE = "value";
		this.recordReturn(this.context, this.context.getInitParameter(
				this.office, NAME), VALUE);

		// Record obtain init parameter names
		final Enumeration<?> ENUMERATION = this.createMock(Enumeration.class);
		this.recordReturn(this.context, this.context
				.getInitParameterNames(this.office), ENUMERATION);

		// Test
		this.replayMockObjects();
		assertEquals("Incorrect init parameter value", "value",
				this.servletContext.getInitParameter("name"));
		assertEquals("Incorrect init parameter enumeration", ENUMERATION,
				this.servletContext.getInitParameterNames());
		this.verifyMockObjects();
	}

	/**
	 * Ensure can set, obtain and remove attributes.
	 */
	public void testAttributes() {
		final String NAME = "name";
		final Object VALUE = new Object();

		// Record specify attribute
		this.context.setAttribute(this.office, NAME, VALUE);

		// Record obtain attribute
		this.recordReturn(this.context, this.context.getAttribute(this.office,
				NAME), VALUE);

		// Record obtain attribute names
		final Enumeration<?> ENUMERATION = this.createMock(Enumeration.class);
		this.recordReturn(this.context, this.context
				.getAttributeNames(this.office), ENUMERATION);

		// Record remove attribute
		this.context.removeAttribute(this.office, NAME);

		// Test
		this.replayMockObjects();
		this.servletContext.setAttribute(NAME, VALUE);
		assertEquals("Incorrect attribute", VALUE, this.servletContext
				.getAttribute(NAME));
		assertEquals("Incorrect attribute name enumeration", ENUMERATION,
				this.servletContext.getAttributeNames());
		this.servletContext.removeAttribute(NAME);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain servlet context name.
	 */
	public void testServletContextName() {

		// Record obtain Servlet Context name
		final String SERVLET_CONTEXT_NAME = "Servlet Context";
		this.recordReturn(this.context, this.context
				.getServletContextName(this.office), SERVLET_CONTEXT_NAME);

		// Test
		this.replayMockObjects();
		assertEquals("Incorrect servlet context name", SERVLET_CONTEXT_NAME,
				this.servletContext.getServletContextName());
		this.verifyMockObjects();
	}

}