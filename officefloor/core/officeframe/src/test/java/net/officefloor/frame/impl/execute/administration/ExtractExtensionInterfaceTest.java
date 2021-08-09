/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.administration;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.extension.ExtensionFactory;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
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
			metaData.addManagedObjectExtension(ManagedObjectExtension.class, new ManagedObjectExtension(null));
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
	public static class ManagedObjectExtension implements ExtensionFactory<ManagedObjectExtension> {

		public ManagedObject managedObject;

		public ManagedObjectExtension(ManagedObject managedObject) {
			this.managedObject = managedObject;
		}

		@Override
		public ManagedObjectExtension createExtension(ManagedObject managedObject) {
			assertNotNull("Must be provided managed object", managedObject);
			return new ManagedObjectExtension(managedObject);
		}
	}

}
