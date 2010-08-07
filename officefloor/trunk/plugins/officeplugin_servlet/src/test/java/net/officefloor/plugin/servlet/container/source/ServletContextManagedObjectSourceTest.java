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
package net.officefloor.plugin.servlet.container.source;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Set;

import javax.servlet.ServletContext;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;

/**
 * Tests the {@link ServletContextManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletContextManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Properties for sourcing.
	 */
	private static final String[] PROPERTIES = new String[] { "server.name",
			"www.officefloor.net", "server.port", "80", "servlet.context.name",
			"ServletContextName", "context.path", "/context",
			"init.parameter.test", "Init Parameter", "resource.path.root",
			null, "file.ext.to.mime.type.custom", "text/custom" };

	@Override
	protected void setUp() throws Exception {
		// Specify location of resource path root
		PROPERTIES[11] = this.findFile(this.getClass(), ".").getAbsolutePath();
	}

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(
						ServletContextManagedObjectSource.class,
						ServletContextManagedObjectSource.PROPERTY_SERVER_NAME,
						"Server Name",
						ServletContextManagedObjectSource.PROPERTY_SERVLET_CONTEXT_NAME,
						"Servlet Context Name",
						ServletContextManagedObjectSource.PROPERTY_CONTEXT_PATH,
						"Context Path",
						ServletContextManagedObjectSource.PROPERTY_RESOURCE_PATH_ROOT,
						"Resource Path Root");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(ServletContext.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				ServletContextManagedObjectSource.class, PROPERTIES);
	}

	/**
	 * Ensures that the {@link ServletContext} is a single instance and
	 * correctly initialised.
	 */
	@SuppressWarnings("unchecked")
	public void testServletContext() throws Throwable {

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		for (int i = 0; i < PROPERTIES.length; i += 2) {
			loader.addProperty(PROPERTIES[i], PROPERTIES[i + 1]);
		}
		ServletContextManagedObjectSource mos = loader
				.loadManagedObjectSource(ServletContextManagedObjectSource.class);

		// Obtain a managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject mo = user.sourceManagedObject(mos);

		// Obtain the Servlet Context and validate configured
		ServletContext servletContext = (ServletContext) mo.getObject();
		assertEquals("getServletContextName", "ServletContextName",
				servletContext.getServletContextName());
		assertEquals("getContextPath", "/context", servletContext
				.getContextPath());
		assertEquals("getInitParameter(test)", "Init Parameter", servletContext
				.getInitParameter("test"));
		Set<String> resources = servletContext.getResourcePaths("test");
		assertTrue("getResourcePaths", resources.contains("test.txt"));
		assertEquals("getRealPath(test.html)",
				"http://www.officefloor.net/context/test.html", servletContext
						.getRealPath("test.html"));

		// Validate the logging
		PrintStream stdOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setOut(new PrintStream(output));
			servletContext.log("test");
			assertTextEquals("log(test)", "test\n", output.toString());
		} finally {
			System.setOut(stdOut);
		}

		// Validate default MIME mappings
		assertEquals("getMimeType(test.css)", "text/css", servletContext
				.getMimeType("text.css"));
		assertEquals("getMimeType(test.gif)", "image/gif", servletContext
				.getMimeType("text.gif"));
		assertEquals("getMimeType(test.htm)", "text/html", servletContext
				.getMimeType("text.htm"));
		assertEquals("getMimeType(test.html)", "text/html", servletContext
				.getMimeType("text.html"));
		assertEquals("getMimeType(test.ico)", "image/x-icon", servletContext
				.getMimeType("text.ico"));
		assertEquals("getMimeType(test.jpeg)", "image/jpeg", servletContext
				.getMimeType("text.jpeg"));
		assertEquals("getMimeType(test.jpg)", "image/jpeg", servletContext
				.getMimeType("text.jpg"));
		assertEquals("getMimeType(test.js)", "application/x-javascript",
				servletContext.getMimeType("text.js"));
		assertEquals("getMimeType(test.log)", "text/plain", servletContext
				.getMimeType("text.log"));
		assertEquals("getMimeType(test.pdf)", "application/pdf", servletContext
				.getMimeType("text.pdf"));
		assertEquals("getMimeType(test.png)", "image/png", servletContext
				.getMimeType("text.png"));
		assertEquals("getMimeType(test.txt)", "text/plain", servletContext
				.getMimeType("text.txt"));
		assertEquals("getMimeType(test.xml)", "text/xml", servletContext
				.getMimeType("text.xml"));

		// Validate custom MIME mapping
		assertEquals("getMimeType(test.custom)", "text/custom", servletContext
				.getMimeType("text.custom"));

		// Test attributes (for re-obtaining context)
		final Object attribute = new Object();
		servletContext.setAttribute("test", attribute);
		assertEquals("Incorrect attribute", attribute, servletContext
				.getAttribute("test"));

		// Ensure same servlet config each time
		ManagedObject otherMo = user.sourceManagedObject(mos);
		ServletContext otherServletContext = (ServletContext) otherMo
				.getObject();
		assertSame("Must be same ServletContext each time", servletContext,
				otherServletContext);

		// Ensure attribute available across sourcing
		assertEquals("Incorrect attribute accross sourcing", attribute,
				otherServletContext.getAttribute("test"));
	}

}