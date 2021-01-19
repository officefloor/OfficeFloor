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

package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure can enforce the {@link Governance}.
 *
 * @author Daniel Sagenschneider
 */
public class AsynchronousGovernanceEnforceTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can enforce {@link Governance} invoking {@link AsynchronousFlow}.
	 */
	public void testAsynchronousGovernance() throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (context) -> context.addManagedObjectExtension(TestObject.class,
				(managedObject) -> (TestObject) managedObject);
		object.isRecycleFunction = true;

		// Construct the function
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO", ManagedObjectScope.THREAD).mapGovernance("GOVERNANCE");
		task.getBuilder().addGovernance("GOVERNANCE");

		// Provide governance
		TestGovernance govern = new TestGovernance();
		ReflectiveGovernanceBuilder governance = this.constructGovernance(govern, "GOVERNANCE");
		governance.enforce("enforce").buildAsynchronousFlow();

		// Invoke the function
		Closure<Throwable> escalation = new Closure<>();
		Closure<Boolean> isComplete = new Closure<>(false);
		this.setRecordReflectiveFunctionMethodsInvoked(true);
		this.triggerFunction("task", null, (error) -> {
			escalation.value = error;
			isComplete.value = true;
		});
		assertNull("Should be no escalation: " + escalation.value, escalation.value);
		assertFalse("Should not be complete", isComplete.value);

		// Ensure can complete
		govern.flow.complete(null);
		assertNull("Should complete with no escalation: " + escalation.value, escalation.value);
		assertTrue("Should be complete", isComplete.value);
		this.validateReflectiveMethodOrder("task", "enforce");
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void task(TestObject object) {
			// Testing
		}
	}

	/**
	 * Test {@link Governance}.
	 */
	public class TestGovernance {

		private AsynchronousFlow flow;

		public void enforce(TestObject[] extensions, AsynchronousFlow flow) {
			this.flow = flow;
		}
	}

}
