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

package net.officefloor.frame.stress.administration;

import junit.framework.TestSuite;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(AdministrationStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct the managed object
		this.constructManagedObject("MO",
				(metaData) -> metaData.addManagedObjectExtension(AdministeredManagedObject.class,
						(managedObject) -> (AdministeredManagedObject) managedObject),
				() -> new AdministeredManagedObject());

		// Construct the functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		context.loadResponsibleTeam(task.getBuilder());
		task.buildObject("MO", ManagedObjectScope.PROCESS);
		task.setNextFunction("next");
		task.preAdminister("preAdminister").administerManagedObject("MO");
		task.postAdminister("postAdminister").administerManagedObject("MO");
		ReflectiveFunctionBuilder next = this.constructFunction(work, "next");
		context.loadOtherTeam(next.getBuilder());
		next.buildObject("MO");
		next.buildFlow("task", null, false);

		// Test
		context.setInitialFunction("task", null);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void preAdminister(AdministeredManagedObject[] extensions) {
			extensions[0].isPreAdministered = true;
		}

		public void task(AdministeredManagedObject object) {
			assertTrue("Should be pre-administered", object.isPreAdministered);
			assertFalse("Should not have been post-administered", object.isPostAdministered);
		}

		public void postAdminister(AdministeredManagedObject[] extensions) {
			extensions[0].isPostAdministered = true;
		}

		public void next(AdministeredManagedObject object, ReflectiveFlow task) {
			assertTrue("Should be pre-administered", object.isPreAdministered);
			assertTrue("Should also be post-adminsitered", object.isPostAdministered);

			// Reset
			object.isPreAdministered = false;
			object.isPostAdministered = false;

			// Determine if continue
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Repeat
			task.doFlow(null, null);
		}
	}

	/**
	 * Administered {@link ManagedObject}.
	 */
	public class AdministeredManagedObject implements ManagedObject {

		private boolean isPreAdministered = false;

		private boolean isPostAdministered = false;

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}
