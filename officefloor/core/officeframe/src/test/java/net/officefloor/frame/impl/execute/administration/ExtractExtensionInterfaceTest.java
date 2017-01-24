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
package net.officefloor.frame.impl.execute.administration;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionInterfaceFactory;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure extract the extension interface from the {@link ManagedObject}
 * instances.
 *
 * @author Daniel Sagenschneider
 */
public class ExtractExtensionInterfaceTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure extract {@link ProcessState} bound {@link ManagedObject}.
	 */
	public void testExtractProcessBoundManagedObjectExtension() throws Exception {
		this.doExtractExtensionTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure extract {@link ThreadState} bound {@link ManagedObject}.
	 */
	public void testExtractThreadBoundManagedObjectExtension() throws Exception {
		this.doExtractExtensionTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure extract {@link ManagedFunction} bound {@link ManagedObject}.
	 */
	public void testExtractFunctionBoundManagedObjectExtension() throws Exception {
		this.doExtractExtensionTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure able to extract extension interface from {@link ManagedObject}.
	 * 
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 */
	public void doExtractExtensionTest(ManagedObjectScope scope) throws Exception {

		// Track method invocations
		this.setRecordReflectiveFunctionMethodsInvoked(true);

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (metaData) -> {
			metaData.addManagedObjectExtensionInterface(ManagedObjectExtension.class, new ManagedObjectExtension(null));
		};

		// Construct the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");

		// Register the managed object to office
		switch (scope) {
		case PROCESS:
			this.getOfficeBuilder().addProcessManagedObject("MO", "MO");
			break;

		case THREAD:
			this.getOfficeBuilder().addThreadManagedObject("MO", "MO");
			break;

		case FUNCTION:
			task.getBuilder().addManagedObject("MO", "MO");
			break;
		}

		// Construct the administration
		task.preAdminister("preTask").administerManagedObject("MO");

		// Invoke function
		this.invokeFunction("task", null);

		// Ensure correct methods invoked
		this.validateReflectiveMethodOrder("preTask", "task");

		// Ensure correct extension extracted
		assertEquals("Incorrect number of extensions administered", 1, work.extensions.length);
		assertSame("Incorrect extension", object, work.extensions[0].managedObject);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		private ManagedObjectExtension[] extensions;

		public void preTask(ManagedObjectExtension[] extensions) {
			this.extensions = extensions;
		}

		public void task() {
			assertEquals("Should have pre-administration", 1, this.extensions.length);
		}
	}

	/**
	 * Extension for the {@link ManagedObject}.
	 */
	public static class ManagedObjectExtension implements ExtensionInterfaceFactory<ManagedObjectExtension> {

		public ManagedObject managedObject;

		public ManagedObjectExtension(ManagedObject managedObject) {
			this.managedObject = managedObject;
		}

		@Override
		public ManagedObjectExtension createExtensionInterface(ManagedObject managedObject) {
			assertNotNull("Must be provided managed object", managedObject);
			return new ManagedObjectExtension(managedObject);
		}
	}

}