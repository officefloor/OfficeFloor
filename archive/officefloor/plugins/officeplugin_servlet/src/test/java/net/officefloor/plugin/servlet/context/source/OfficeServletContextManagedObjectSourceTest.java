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
package net.officefloor.plugin.servlet.context.source;

import javax.servlet.ServletContext;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.servlet.context.OfficeServletContext;
import net.officefloor.plugin.servlet.context.source.OfficeServletContextManagedObjectSource.DependencyKeys;
import net.officefloor.plugin.servlet.host.ServletServer;

/**
 * Tests the {@link OfficeServletContextManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeServletContextManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(
						OfficeServletContextManagedObjectSource.class,
						OfficeServletContextManagedObjectSource.PROPERTY_SERVLET_CONTEXT_NAME,
						"Servlet Context Name");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(OfficeServletContext.class);
		type.addDependency(DependencyKeys.SERVLET_SERVER, ServletServer.class,
				null);

		// Validate type
		ManagedObjectLoaderUtil
				.validateManagedObjectType(
						type,
						OfficeServletContextManagedObjectSource.class,
						OfficeServletContextManagedObjectSource.PROPERTY_SERVLET_CONTEXT_NAME,
						"Test");
	}

	/**
	 * Ensures that the {@link ServletContext} is a single instance and
	 * correctly initialised.
	 */
	public void testSource() throws Throwable {

		final Office office = this.createMock(Office.class);
		final ServletServer servletServer = this
				.createMock(ServletServer.class);

		// Record
		this.recordReturn(servletServer, servletServer.getContextPath(),
				"/context");
		this.recordReturn(office, office.getWorkNames(), new String[0]);

		// Test
		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				OfficeServletContextManagedObjectSource.PROPERTY_SERVLET_CONTEXT_NAME,
				"Servlet Context Name");
		loader.addProperty(
				OfficeServletContextManagedObjectSource.PROPERTY_PREFIX_INIT_PARAMETER
						+ "test", "Init Parameter");
		loader.addProperty(
				OfficeServletContextManagedObjectSource.PROPERTY_PREFIX_FILE_EXTENSION_TO_MIME_TYPE
						+ "custom", "text/custom");
		OfficeServletContextManagedObjectSource mos = loader
				.loadManagedObjectSource(OfficeServletContextManagedObjectSource.class);

		// Obtain a managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(DependencyKeys.SERVLET_SERVER, servletServer);
		ManagedObject mo = user.sourceManagedObject(mos);

		// Obtain the Office Servlet Context and validate configured
		OfficeServletContext context = (OfficeServletContext) mo.getObject();
		assertEquals("getServletContextName", "Servlet Context Name",
				context.getServletContextName(office));
		assertEquals("getContextPath", "/context",
				context.getContextPath(office));
		assertEquals("getInitParameter(test)", "Init Parameter",
				context.getInitParameter(office, "test"));

		// Validate default MIME mappings
		assertEquals("getMimeType(test.css)", "text/css",
				context.getMimeType(office, "text.css"));
		assertEquals("getMimeType(test.gif)", "image/gif",
				context.getMimeType(office, "text.gif"));
		assertEquals("getMimeType(test.htm)", "text/html",
				context.getMimeType(office, "text.htm"));
		assertEquals("getMimeType(test.html)", "text/html",
				context.getMimeType(office, "text.html"));
		assertEquals("getMimeType(test.ico)", "image/x-icon",
				context.getMimeType(office, "text.ico"));
		assertEquals("getMimeType(test.jpeg)", "image/jpeg",
				context.getMimeType(office, "text.jpeg"));
		assertEquals("getMimeType(test.jpg)", "image/jpeg",
				context.getMimeType(office, "text.jpg"));
		assertEquals("getMimeType(test.js)", "application/x-javascript",
				context.getMimeType(office, "text.js"));
		assertEquals("getMimeType(test.log)", "text/plain",
				context.getMimeType(office, "text.log"));
		assertEquals("getMimeType(test.pdf)", "application/pdf",
				context.getMimeType(office, "text.pdf"));
		assertEquals("getMimeType(test.png)", "image/png",
				context.getMimeType(office, "text.png"));
		assertEquals("getMimeType(test.txt)", "text/plain",
				context.getMimeType(office, "text.txt"));
		assertEquals("getMimeType(test.xml)", "text/xml",
				context.getMimeType(office, "text.xml"));

		// Validate custom MIME mapping
		assertEquals("getMimeType(test.custom)", "text/custom",
				context.getMimeType(office, "text.custom"));

		// Test attributes (for re-obtaining context)
		final Object attribute = new Object();
		context.setAttribute(office, "test", attribute);
		assertEquals("Incorrect attribute", attribute,
				context.getAttribute(office, "test"));

		// Ensure same servlet config each time
		ManagedObject otherMo = user.sourceManagedObject(mos);
		OfficeServletContext otherContext = (OfficeServletContext) otherMo
				.getObject();
		assertSame("Must be same ServletContext each time", context,
				otherContext);

		// Ensure attribute available across sourcing
		assertEquals("Incorrect attribute accross sourcing", attribute,
				otherContext.getAttribute(office, "test"));

		this.verifyMockObjects();
	}

}