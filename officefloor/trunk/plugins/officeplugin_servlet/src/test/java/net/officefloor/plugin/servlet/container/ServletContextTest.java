/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.dispatch.RequestDispatcherFactory;
import net.officefloor.plugin.servlet.log.Logger;
import net.officefloor.plugin.servlet.resource.ResourceLocator;

/**
 * Tests the {@link ServletContextImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletContextTest extends OfficeFrameTestCase {

	/**
	 * Context path.
	 */
	private final String contextPath = "/context/path";

	/**
	 * Name of the {@link ServletContext}.
	 */
	private final String servletContextName = "ServletContextName";

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
	 * {@link RequestDispatcherFactory}.
	 */
	private final RequestDispatcherFactory dispatcherFactory = this
			.createMock(RequestDispatcherFactory.class);

	/**
	 * {@link Logger}.
	 */
	private final Logger logger = this.createMock(Logger.class);

	/**
	 * {@link HttpServletRequest}.
	 */
	private final HttpServletRequest request = this
			.createMock(HttpServletRequest.class);

	/**
	 * Init parameters.
	 */
	private final Map<String, String> initParameters = new HashMap<String, String>();

	/**
	 * {@link ContextAttributes}.
	 */
	private final ContextAttributes attributes = this
			.createMock(ContextAttributes.class);

	/**
	 * {@link ServletContextImpl} to test.
	 */
	private final ServletContext context = new ServletContextImpl(
			this.servletContextName, this.contextPath, this.mimeMappings,
			this.locator, this.dispatcherFactory, this.logger, this.request,
			this.initParameters, this.attributes);

	/**
	 * Ensure correct context path.
	 */
	public void testContextPath() {
		assertEquals("Incorrect context path", this.contextPath, this.context
				.getContextPath());
	}

	/**
	 * Due to security (and simplicity) can not obtain other
	 * {@link ServletContext} instances.
	 */
	public void testOtherContexts() {
		assertNull("Restricting access to other servlet contexts", this.context
				.getContext("/other"));
	}

	/**
	 * Ensure correct version supported.
	 */
	public void testVersion() {
		assertEquals("Incorrect major version", 2, this.context
				.getMajorVersion());
		assertEquals("Incorrect minor version", 5, this.context
				.getMinorVersion());
	}

	/**
	 * Ensure correct MIME type for files.
	 */
	public void testFileMimeTypes() {
		this.mimeMappings.put("html", "text/html");
		assertNull("No extension so no MIME type", this.context
				.getMimeType("NoExtensionFileName"));
		assertEquals("Incorrect MIME type", "text/html", this.context
				.getMimeType("file.html"));
		assertNull("Unknown file extension", this.context
				.getMimeType("file.unknown"));
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
				.getResourcePaths("/parent"));
		assertEquals("Incorrect resource URL", url, this.context
				.getResource("/resource"));
		assertEquals("Incorrect resource InputStream", stream, this.context
				.getResourceAsStream("/resource"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can create {@link RequestDispatcher}.
	 */
	public void testRequestDispatcher() {
		final RequestDispatcher dispatcher = this
				.createMock(RequestDispatcher.class);
		this.recordReturn(this.dispatcherFactory, this.dispatcherFactory
				.createRequestDispatcher("/resource"), dispatcher);
		this.recordReturn(this.dispatcherFactory, this.dispatcherFactory
				.createNamedDispatcher("Name"), dispatcher);
		this.replayMockObjects();
		assertEquals("Incorrect request dispatcher", dispatcher, this.context
				.getRequestDispatcher("/resource"));
		assertEquals("Incorrect named dispatcher", dispatcher, this.context
				.getNamedDispatcher("Name"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can log.
	 */
	public void testLogging() {
		final String message = "Log Message";
		final Throwable failure = new Throwable("test");
		this.logger.log(message);
		this.logger.log(message, failure);
		this.replayMockObjects();
		this.context.log(message);
		this.context.log(message, failure);
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain real path.
	 */
	public void testRealPath() {
		this.recordReturn(this.request, this.request.getScheme(), "http");
		this.recordReturn(this.request, this.request.getLocalName(),
				"officefloor.net");
		this.recordReturn(this.request, this.request.getLocalPort(), 80);
		this.replayMockObjects();
		assertEquals("Incorrect real path", "http://officefloor.net:80"
				+ this.contextPath + "/real.path", this.context
				.getRealPath("/real.path"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure correct server info.
	 */
	public void testServerInfo() {
		String serverInfo = "OfficeFloor servlet plug-in/1.0";
		System.out.println("Validating server info to be '" + serverInfo + "'");
		assertEquals("Incorrect server info", serverInfo, this.context
				.getServerInfo());
	}

	/**
	 * Ensure can obtain init parameters.
	 */
	public void testInitParameters() {
		this.initParameters.put("name", "value");
		assertEquals("Incorrect init parameter value", "value", this.context
				.getInitParameter("name"));
		Enumeration<?> enumeration = this.context.getInitParameterNames();
		assertTrue("Expecting init parameter name", enumeration
				.hasMoreElements());
		assertEquals("Incorrect init parameter name", "name", enumeration
				.nextElement());
		assertFalse("Expecting only one init parameter name", enumeration
				.hasMoreElements());
	}

	/**
	 * Ensure can set, obtain and remove attributes.
	 */
	@SuppressWarnings("unchecked")
	public void testAttributes() {
		final Object attribute = new Object();

		// Record setting, obtaining, removing an attribute
		this.attributes.setAttribute("attribute", attribute);
		this.recordReturn(this.attributes, this.attributes
				.getAttribute("attribute"), attribute);
		this.recordReturn(this.attributes, this.attributes.getAttributeNames(),
				Arrays.asList("attribute").iterator());
		this.attributes.removeAttribute("attribute");
		this.recordReturn(this.attributes, this.attributes
				.getAttribute("attribute"), null);

		// Test
		this.replayMockObjects();
		this.context.setAttribute("attribute", attribute);
		assertEquals("Incorrect attribute", attribute, this.context
				.getAttribute("attribute"));
		Enumeration<String> enumeration = this.context.getAttributeNames();
		assertTrue("Expecting an attribute name", enumeration.hasMoreElements());
		assertEquals("Incorrect attribute name", "attribute", enumeration
				.nextElement());
		assertFalse("Expecting only one attribute name", enumeration
				.hasMoreElements());
		this.context.removeAttribute("attribute");
		assertNull("Attribute should be removed", this.context
				.getAttribute("attribute"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain servlet context name.
	 */
	public void testServletContextName() {
		assertEquals("Incorrect servlet context name", this.servletContextName,
				this.context.getServletContextName());
	}

}