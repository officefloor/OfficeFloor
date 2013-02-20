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
package net.officefloor.plugin.servlet;

import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.servlet.ServletDependencyManagedObjectSource.DependencyKeys;
import net.officefloor.plugin.servlet.bridge.ServletBridge;

/**
 * Tests the {@link ServletDependencyManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletDependencyManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(
						ServletDependencyManagedObjectSource.class,
						ServletDependencyManagedObjectSource.PROPERTY_TYPE_NAME,
						"Type");
	}

	/**
	 * Validate type.
	 */
	public void testType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(MockEjbLocal.class);
		type.addDependency(DependencyKeys.SERVLET_BRIDGE, ServletBridge.class,
				null);

		// Validate type
		ManagedObjectLoaderUtil.validateManagedObjectType(type,
				ServletDependencyManagedObjectSource.class,
				ServletDependencyManagedObjectSource.PROPERTY_TYPE_NAME,
				MockEjbLocal.class.getName());
	}

	/**
	 * Ensure can source.
	 */
	public void testSource() throws Throwable {

		final ServletBridge bridge = this.createMock(ServletBridge.class);
		final MockEjbLocal ejb = this.createMock(MockEjbLocal.class);

		// Record obtaining the dependency
		this.recordReturn(bridge, bridge.getObject(MockEjbLocal.class), ejb);
		this.replayMockObjects();

		// Load the source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				ServletDependencyManagedObjectSource.PROPERTY_TYPE_NAME,
				MockEjbLocal.class.getName());
		ServletDependencyManagedObjectSource source = loader
				.loadManagedObjectSource(ServletDependencyManagedObjectSource.class);

		// Source the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(DependencyKeys.SERVLET_BRIDGE, bridge);
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Ensure correct object
		Object object = managedObject.getObject();
		assertSame("Incorrect dependency", ejb, object);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Mock dependency type.
	 */
	public static interface MockEjbLocal {
	}

}