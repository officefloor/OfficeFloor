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
package net.officefloor.plugin.servlet.socket.server.http.source;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.servlet.bridge.ServletBridge;
import net.officefloor.plugin.servlet.socket.server.http.source.ServletServerHttpConnectionManagedObjectSource.DependencyKeys;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;

/**
 * Tests the {@link ServletServerHttpConnectionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServerHttpConnectionManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(ServletServerHttpConnectionManagedObjectSource.class);
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(ServerHttpConnection.class);
		type.addDependency(DependencyKeys.SERVLET_BRIDGE, ServletBridge.class,
				null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				ServletServerHttpConnectionManagedObjectSource.class);
	}

	/**
	 * Validate source.
	 */
	public void testSource() throws Throwable {

		final ServletBridge bridge = this.createMock(ServletBridge.class);
		final HttpServletRequest request = this
				.createMock(HttpServletRequest.class);
		final HttpServletResponse response = this
				.createMock(HttpServletResponse.class);

		// Record
		this.recordReturn(bridge, bridge.getRequest(), request);
		this.recordReturn(bridge, bridge.getResponse(), response);
		this.recordReturn(request, request.isSecure(), true);

		// Test
		this.replayMockObjects();

		// Load the source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		ServletServerHttpConnectionManagedObjectSource source = loader
				.loadManagedObjectSource(ServletServerHttpConnectionManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(DependencyKeys.SERVLET_BRIDGE, bridge);
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Obtain the object
		ServerHttpConnection object = (ServerHttpConnection) managedObject
				.getObject();

		// Ensure appropriately configured
		assertTrue("Should be secure", object.isSecure());
		this.verifyMockObjects();
	}

}