/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
