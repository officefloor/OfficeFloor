/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.servlet.host;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.servlet.host.ServletServer;
import net.officefloor.plugin.servlet.host.ServletServerManagedObjectSource;
import net.officefloor.plugin.servlet.host.ServletServerManagedObjectSource.Dependencies;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;

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
				"Server Name");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() throws Exception {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(ServletServer.class);
		type.addDependency(Dependencies.HTTP_APPLICATION_LOCATION,
				HttpApplicationLocation.class, null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				ServletServerManagedObjectSource.class,
				ServletServerManagedObjectSource.PROPERTY_SERVER_NAME,
				"officefloor.net");
	}

	/**
	 * Ensure correctly sourced.
	 */
	public void testSource() throws Throwable {

		final String SERVER_NAME = "officefloor.net";
		final int SERVER_PORT = 80;
		final String CONTEXT_PATH = "/context";

		final HttpApplicationLocation location = this
				.createMock(HttpApplicationLocation.class);

		// Obtain resource path root
		final String RESOURCE_NAME = "resource.txt";

		// Record obtaining location details
		this.recordReturn(location, location.getHttpPort(), SERVER_PORT);
		this.recordReturn(location, location.getContextPath(), CONTEXT_PATH);

		// Record to default context path
		this.recordReturn(location, location.getContextPath(), null);

		// Test
		this.replayMockObjects();

		// Load the source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				ServletServerManagedObjectSource.PROPERTY_SERVER_NAME,
				SERVER_NAME);
		loader.addProperty(
				ServletServerManagedObjectSource.PROPERTY_CLASS_PATH_PREFIX,
				this.getClass().getPackage().getName());
		ServletServerManagedObjectSource source = loader
				.loadManagedObjectSource(ServletServerManagedObjectSource.class);

		// Obtain the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(Dependencies.HTTP_APPLICATION_LOCATION, location);
		ManagedObject managedObject = user.sourceManagedObject(source);
		assertNotNull("Ensure have managed object", managedObject);

		// Obtain the object
		Object object = managedObject.getObject();
		assertTrue("Ensure servlet server instance",
				object instanceof ServletServer);
		ServletServer server = (ServletServer) object;

		// Validate loaded correctly
		assertEquals("Incorrect server name", SERVER_NAME,
				server.getServerName());
		assertEquals("Incorrect server port", SERVER_PORT,
				server.getServerPort());
		assertEquals("Incorrect context path", CONTEXT_PATH,
				server.getContextPath());
		assertNotNull("Resource locator not configured correctly", server
				.getResourceLocator().getResource(RESOURCE_NAME));

		// Validate default no context path
		assertEquals("Incorrect defaulted context path", "/",
				server.getContextPath());

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

		// Verify
		this.verifyMockObjects();
	}

}