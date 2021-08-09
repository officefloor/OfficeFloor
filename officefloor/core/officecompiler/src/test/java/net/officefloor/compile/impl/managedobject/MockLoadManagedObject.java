/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.impl.managedobject;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourcePropertyType;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

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
	@Qualified("QUALIFIED")
	@Dependency
	Connection connection;

	/**
	 * {@link MockProcessInterface}.
	 */
	MockProcessInterface processes;

	/**
	 * Validates the {@link ManagedObjectType} is correct for this class object.
	 * 
	 * @param managedObjectType {@link ManagedObjectType}
	 */
	public static void assertManagedObjectType(ManagedObjectType<?> managedObjectType) {

		// Ensure correct object type
		Assert.assertEquals("Incorrect object type", MockLoadManagedObject.class, managedObjectType.getObjectType());

		// Ensure input
		Assert.assertTrue("Should be input", managedObjectType.isInput());

		// Ensure correct dependencies
		Assert.assertEquals("Incorrect number of dependencies", 1, managedObjectType.getDependencyTypes().length);
		ManagedObjectDependencyType<?> dependencyType = managedObjectType.getDependencyTypes()[0];
		Assert.assertEquals("Incorrect dependency", Connection.class, dependencyType.getDependencyType());
		Assert.assertEquals("Incorrect dependency qualifier", "QUALIFIED", dependencyType.getTypeQualifier());
		List<Object> dependencyAnnotations = Arrays.asList(dependencyType.getAnnotations());
		Assert.assertEquals("Incorrect number of annotations", 2, dependencyAnnotations.size());
		try {
			Dependency dependencyAnnotation = MockLoadManagedObject.class.getDeclaredField("connection")
					.getAnnotation(Dependency.class);
			Assert.assertTrue("Must have dependency annotation", dependencyAnnotations.contains(dependencyAnnotation));
		} catch (Exception ex) {
			OfficeFrameTestCase.fail(ex);
		}

		// Ensure correct flows
		Assert.assertEquals("Incorrect number of flows", 1, managedObjectType.getFlowTypes().length);
		ManagedObjectFlowType<?> flowType = managedObjectType.getFlowTypes()[0];
		Assert.assertEquals("Incorrect flow name", "doProcess", flowType.getFlowName());
		Assert.assertEquals("Incorrect flow argument type", Integer.class, flowType.getArgumentType());

		// Ensure no teams
		Assert.assertEquals("Incorrect number of teams", 0, managedObjectType.getTeamTypes().length);

		// Ensure no execution strategies
		Assert.assertEquals("Incorrect number of execution strategies", 0,
				managedObjectType.getExecutionStrategyTypes().length);

		// Ensure correct extension interface
		Assert.assertEquals("Incorrect number of extension interfaces", 1,
				managedObjectType.getExtensionTypes().length);
		Assert.assertEquals("Incorrect extension interface", MockLoadManagedObject.class,
				managedObjectType.getExtensionTypes()[0]);
	}

	/**
	 * Validates the {@link OfficeFloorManagedObjectSourceType} is correct for the
	 * class object.
	 * 
	 * @param managedObjectSourceType {@link OfficeFloorManagedObjectSourceType}.
	 */
	public static void assertOfficeFloorManagedObjectSourceType(
			OfficeFloorManagedObjectSourceType managedObjectSourceType, String managedObjectSourceName) {

		// Ensure correct managed object source name
		Assert.assertEquals("Incorrect managed object source name", managedObjectSourceName,
				managedObjectSourceType.getOfficeFloorManagedObjectSourceName());

		// Ensure correct properties
		OfficeFloorManagedObjectSourcePropertyType[] properties = managedObjectSourceType
				.getOfficeFloorManagedObjectSourcePropertyTypes();
		Assert.assertNotNull("Must have properties", properties);
		Assert.assertEquals("Incorrect number of properties", 1, properties.length);
		OfficeFloorManagedObjectSourcePropertyType property = properties[0];
		Assert.assertEquals("Incorrect property name", ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME,
				property.getName());
		Assert.assertEquals("Incorrect property label", "Class", property.getLabel());
		Assert.assertEquals("Incorrect property default value", MockLoadManagedObject.class.getName(),
				property.getDefaultValue());
	}

}
