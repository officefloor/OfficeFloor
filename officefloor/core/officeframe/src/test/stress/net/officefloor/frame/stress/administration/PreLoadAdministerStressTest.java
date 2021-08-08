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
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress test pre-load {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public class PreLoadAdministerStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(PreLoadAdministerStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct the managed object
		this.constructManagedObject(new AdministeredManagedObject(), "MO", this.getOfficeName());

		// Construct the functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		context.loadOtherTeam(task.getBuilder());
		AdministrationBuilder<None, None> admin = task.buildObject("MO", ManagedObjectScope.FUNCTION)
				.preLoadAdminister("ADMIN", Object.class, work);
		context.loadResponsibleTeam(admin);
		task.setNextFunction("next");
		ReflectiveFunctionBuilder next = this.constructFunction(work, "next");
		context.loadOtherTeam(next.getBuilder());
		next.buildFlow("task", null, false);

		// Test
		context.setInitialFunction("task", null);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork implements AdministrationFactory<Object, None, None>, Administration<Object, None, None> {

		private final StressContext context;

		private boolean isPreLoadAdministered = false;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void task(AdministeredManagedObject object) {
			assertTrue("Should pre-load administer managed object", this.isPreLoadAdministered);
		}

		public void next(ReflectiveFlow task) {

			// Reset to ensure administer again
			this.isPreLoadAdministered = false;

			// Determine if continue
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Repeat
			task.doFlow(null, null);
		}

		/*
		 * ==================== Administration ====================
		 */

		@Override
		public Administration<Object, None, None> createAdministration() throws Throwable {
			return this;
		}

		@Override
		public void administer(AdministrationContext<Object, None, None> context) throws Throwable {
			this.isPreLoadAdministered = true;
		}
	}

	/**
	 * {@link ManagedObject}.
	 */
	public class AdministeredManagedObject {
	}

}
