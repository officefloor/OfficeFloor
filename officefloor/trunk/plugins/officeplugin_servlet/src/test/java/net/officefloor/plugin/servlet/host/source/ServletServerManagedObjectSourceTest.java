/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.servlet.host.source;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.servlet.host.ServletServer;

/**
 * Tests the {@link ServletServerManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServerManagedObjectSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil.validateSpecification(
				ServletServerManagedObjectSource.class,
				ServletServerManagedObjectSource.PROPERTY_SERVER_NAME,
				"Server Name",
				ServletServerManagedObjectSource.PROPERTY_SERVER_PORT,
				"Server Port",
				ServletServerManagedObjectSource.PROPERTY_CONTEXT_PATH,
				"Context Path");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() throws Exception {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(ServletServer.class);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				ServletServerManagedObjectSource.class,
				ServletServerManagedObjectSource.PROPERTY_SERVER_NAME,
				"officefloor.net",
				ServletServerManagedObjectSource.PROPERTY_SERVER_PORT, "80",
				ServletServerManagedObjectSource.PROPERTY_CONTEXT_PATH,
				"/context");
	}

	/**
	 * Ensure correctly sourced.
	 */
	public void testSource() throws Throwable {

		final String SERVER_NAME = "officefloor.net";
		final int SERVER_PORT = 80;
		final String CONTEXT_PATH = "/context";

		// Obtain resource path root
		final String RESOURCE_NAME = "resource.txt";

		// Load the source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				ServletServerManagedObjectSource.PROPERTY_SERVER_NAME,
				SERVER_NAME);
		loader.addProperty(
				ServletServerManagedObjectSource.PROPERTY_SERVER_PORT, String
						.valueOf(SERVER_PORT));
		loader.addProperty(
				ServletServerManagedObjectSource.PROPERTY_CONTEXT_PATH,
				CONTEXT_PATH);
		loader.addProperty(
				ServletServerManagedObjectSource.PROPERTY_CLASS_PATH_PREFIX,
				this.getClass().getPackage().getName());
		ServletServerManagedObjectSource source = loader
				.loadManagedObjectSource(ServletServerManagedObjectSource.class);

		// Obtain the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		ManagedObject managedObject = user.sourceManagedObject(source);
		assertNotNull("Ensure have managed object", managedObject);

		// Obtain the object
		Object object = managedObject.getObject();
		assertTrue("Ensure servlet server instance",
				object instanceof ServletServer);
		ServletServer server = (ServletServer) object;

		// Validate loaded correctly
		assertEquals("Incorrect server name", SERVER_NAME, server
				.getServerName());
		assertEquals("Incorrect server port", SERVER_PORT, server
				.getServerPort());
		assertEquals("Incorrect context path", CONTEXT_PATH, server
				.getContextPath());
		assertNotNull("Resource locator not configured correctly", server
				.getResourceLocator().getResource(RESOURCE_NAME));

		// Validate the logging
		PrintStream stdOut = System.out;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			System.setOut(new PrintStream(output));
			server.getLogger().log("test");
			assertTextEquals("log(test)", "test\n", output.toString());
		} finally {
			System.setOut(stdOut);
		}
	}

}