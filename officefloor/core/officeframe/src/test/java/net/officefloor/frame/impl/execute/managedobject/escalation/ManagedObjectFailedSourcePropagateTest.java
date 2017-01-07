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
package net.officefloor.frame.impl.execute.managedobject.escalation;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestManagedObject;

/**
 * Tests failure of sourcing the {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectFailedSourcePropagateTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure propagate failure in sourcing the {@link ManagedFunction} bound
	 * {@link ManagedObject}.
	 */
	public void testFailureInSourcingFunctionBoundManagedObject() throws Exception {
		this.doFailureInSourcingManagedObjectTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure propagate failure in sourcing the {@link ThreadState} bound
	 * {@link ManagedObject}.
	 */
	public void testFailureInSourcingThreadStateBoundManagedObject() throws Exception {
		this.doFailureInSourcingManagedObjectTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure propagate failure in sourcing the {@link ProcessState} bound
	 * {@link ManagedObject}.
	 */
	public void testFailureInSourcingProcessStateBoundManagedObject() throws Exception {
		this.doFailureInSourcingManagedObjectTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param scope
	 *            {@link ManagedObjectScope}.
	 */
	public void doFailureInSourcingManagedObjectTest(ManagedObjectScope scope) throws Exception {

		// Construct the managed object
		TestManagedObject lifeCycle = new TestManagedObject("MO", this);

		// Construct the functionality
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder function = this.constructFunction(work, "task");
		function.buildObject("MO", ManagedObjectScope.FUNCTION);

		// Indicate to failure sourcing the managed object
		lifeCycle.sourceFailure = new Exception("SOURCE_FAILURE");

		// Invoke the function
		try {
			this.invokeFunction("task", null);
			fail("Should propagate managed object source failure");
		} catch (Exception ex) {
			assertSame("Incorrect source failure", lifeCycle.sourceFailure, ex);
		}

		// Ensure life-cycle respected for managed object
		assertFalse("Task should not be invoked", work.isTaskInvoked);
		assertNull("Managed object should not be sourced", work.lifeCycle);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		public boolean isTaskInvoked = false;

		public TestManagedObject lifeCycle = null;

		public void task(TestManagedObject lifeCycle) {
			this.isTaskInvoked = true;
			this.lifeCycle = lifeCycle;
		}
	}

}