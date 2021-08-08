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

package net.officefloor.frame.stress.governance;

import junit.framework.TestSuite;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;

/**
 * Stress tests the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(GovernanceStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct the managed object
		this.constructManagedObject("MO",
				(metaData) -> metaData.addManagedObjectExtension(GovernedManagedObject.class,
						(managedObject) -> (GovernedManagedObject) managedObject),
				() -> new GovernedManagedObject());

		// Construct the functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		context.loadOtherTeam(task.getBuilder());
		task.buildObject("MO", ManagedObjectScope.PROCESS).mapGovernance("GOVERNANCE");
		task.getBuilder().addGovernance("GOVERNANCE");
		task.setNextFunction("next");
		ReflectiveFunctionBuilder next = this.constructFunction(work, "next");
		context.loadOtherTeam(next.getBuilder());
		next.buildObject("MO");
		next.buildFlow("task", null, false);

		// Construct the governance
		TestGovernance governance = new TestGovernance();
		ReflectiveGovernanceBuilder govern = this.constructGovernance(governance, "GOVERNANCE");
		govern.register("register");
		govern.enforce("enforce");
		context.loadResponsibleTeam(govern.getBuilder());

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

		public void task(GovernedManagedObject object) {
			assertTrue("Should be registered", object.isRegistered);
			assertFalse("Should not yet be enforced", object.isEnforced);
		}

		public void next(GovernedManagedObject object, ReflectiveFlow task) {
			assertTrue("Should be registered", object.isRegistered);
			assertTrue("Should also be enforced", object.isEnforced);

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Reset and repeat
			object.isRegistered = true;
			object.isEnforced = false;
			task.doFlow(null, null);
		}
	}

	/**
	 * Test {@link Governance}.
	 */
	public class TestGovernance {

		public void register(GovernedManagedObject extension) {
			extension.isRegistered = true;
		}

		public void enforce(GovernedManagedObject[] extensions) {
			extensions[0].isEnforced = true;
		}
	}

	/**
	 * Governed {@link ManagedObject}.
	 */
	public static class GovernedManagedObject implements ManagedObject {

		private boolean isRegistered = false;

		private boolean isEnforced = false;

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}
