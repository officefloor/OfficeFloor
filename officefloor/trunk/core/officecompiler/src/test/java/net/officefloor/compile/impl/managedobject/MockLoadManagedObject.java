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
package net.officefloor.compile.impl.managedobject;

import java.sql.Connection;

import junit.framework.TestCase;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourcePropertyType;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Class for {@link ClassManagedObjectSource} that enables validating loading a
 * {@link ManagedObjectType}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockLoadManagedObject {

	/**
	 * Mock process interface.
	 */
	@FlowInterface
	public static interface MockProcessInterface {
		void doProcess(Integer parameter);
	}

	/**
	 * {@link Connection} for dependency injection.
	 */
	@Dependency
	Connection connection;

	/**
	 * {@link MockProcessInterface}.
	 */
	MockProcessInterface processes;

	/**
	 * Validates the {@link ManagedObjectType} is correct for this class object.
	 * 
	 * @param managedObjectType
	 *            {@link ManagedObjectType}
	 */
	public static void assertManagedObjectType(
			ManagedObjectType<?> managedObjectType) {

		// Ensure correct object type
		TestCase.assertEquals("Incorrect object type",
				MockLoadManagedObject.class, managedObjectType.getObjectClass());

		// Ensure correct dependencies
		TestCase.assertEquals("Incorrect number of dependencies", 1,
				managedObjectType.getDependencyTypes().length);
		TestCase.assertEquals("Incorrect dependency", Connection.class,
				managedObjectType.getDependencyTypes()[0].getDependencyType());

		// Ensure correct flows
		TestCase.assertEquals("Incorrect number of flows", 1,
				managedObjectType.getFlowTypes().length);
		ManagedObjectFlowType<?> flowType = managedObjectType.getFlowTypes()[0];
		TestCase.assertEquals("Incorrect flow name", "doProcess",
				flowType.getFlowName());
		TestCase.assertEquals("Incorrect flow argument type", Integer.class,
				flowType.getArgumentType());

		// Ensure no teams
		TestCase.assertEquals("Incorrect number of teams", 0,
				managedObjectType.getTeamTypes().length);

		// Ensure correct extension interface
		TestCase.assertEquals("Incorrect number of extension interfaces", 1,
				managedObjectType.getExtensionInterfaces().length);
		TestCase.assertEquals("Incorrect extension interface",
				MockLoadManagedObject.class,
				managedObjectType.getExtensionInterfaces()[0]);
	}

	/**
	 * Validates the {@link OfficeFloorManagedObjectSourceType} is correct for
	 * the class object.
	 * 
	 * @param managedObjectSourceType
	 *            {@link OfficeFloorManagedObjectSourceType}.
	 */
	public static void assertOfficeFloorManagedObjectSourceType(
			OfficeFloorManagedObjectSourceType managedObjectSourceType,
			String managedObjectSourceName) {

		// Ensure correct managed object source name
		TestCase.assertEquals("Incorrect managed object source name",
				managedObjectSourceName,
				managedObjectSourceType.getOfficeFloorManagedObjectSourceName());

		// Ensure correct properties
		OfficeFloorManagedObjectSourcePropertyType[] properties = managedObjectSourceType
				.getOfficeFloorManagedObjectSourcePropertyTypes();
		TestCase.assertNotNull("Must have properties", properties);
		TestCase.assertEquals("Incorrect number of properties", 1,
				properties.length);
		OfficeFloorManagedObjectSourcePropertyType property = properties[0];
		TestCase.assertEquals("Incorrect property name",
				ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				property.getName());
		TestCase.assertEquals("Incorrect property label", "Class",
				property.getLabel());
		TestCase.assertEquals("Incorrect property default value",
				MockLoadManagedObject.class.getName(),
				property.getDefaultValue());
	}

}