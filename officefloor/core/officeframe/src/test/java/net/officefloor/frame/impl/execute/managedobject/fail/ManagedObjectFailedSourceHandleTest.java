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
package net.officefloor.frame.impl.execute.managedobject.fail;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.impl.execute.managedobject.LifeCycleObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure that continues to propagate sourcing failure of {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class ManagedObjectFailedSourceHandleTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link LifeCycleObject}.
	 */
	private LifeCycleObject lifeCycle;

	/**
	 * Ensure {@link Escalation} kept to scope of {@link ManagedFunction}.
	 */
	public void testFunctionBoundSourceEscalation() throws Exception {
		this.doEnsureEscalationTest(ManagedObjectScope.FUNCTION, false);
	}

	/**
	 * Ensure {@link Escalation} kept to scope of {@link ManagedFunction} with
	 * spawned {@link ThreadState} {@link Flow}.
	 */
	public void testFunctionBoundSourceEscalationWithSpawnedThreadStateFlow() throws Exception {
		this.doEnsureEscalationTest(ManagedObjectScope.FUNCTION, true);
	}

	/**
	 * Ensure {@link Escalation} kept to scope of {@link ThreadState}.
	 */
	public void testThreadStateBoundSourceEscalation() throws Exception {
		this.doEnsureEscalationTest(ManagedObjectScope.THREAD, false);
	}

	/**
	 * Ensure {@link Escalation} kept to scope of {@link ThreadState} with
	 * spawned {@link ThreadState} {@link Flow}.
	 */
	public void testThreadStateBoundSourceEscalationWithSpawnedThreadStateFlow() throws Exception {
		this.doEnsureEscalationTest(ManagedObjectScope.THREAD, true);
	}

	/**
	 * Ensure {@link Escalation} kept to scope of {@link ProcessState}.
	 */
	public void testProcessStateBoundSourceEscalation() throws Exception {
		this.doEnsureEscalationTest(ManagedObjectScope.PROCESS, false);
	}

	/**
	 * Ensure {@link Escalation} kept to scope of {@link ProcessState} with
	 * spawned {@link ThreadState} {@link Flow}.
	 */
	public void testProcessStateBoundSourceEscalationWithSpawnedThreadStateFlow() throws Exception {
		this.doEnsureEscalationTest(ManagedObjectScope.PROCESS, true);
	}

	/**
	 * Ensure appropriate {@link Escalation} for sourcing {@link ManagedObject}.
	 */
	public void doEnsureEscalationTest(ManagedObjectScope scope, boolean isSpawnThreadState) throws Exception {

		// Managed object
		this.lifeCycle = new LifeCycleObject("MO", this);

		// Provide the escalation
		Exception escalation = new Exception("SOURCE_FAILURE");
		this.lifeCycle.sourceFailure = escalation;

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.getBuilder().setNextFunction("next", null);
		task.buildFlow("flow", null, isSpawnThreadState);
		ReflectiveFunctionBuilder flow = this.constructFunction(work, "flow");
		flow.buildObject("MO");
		flow.getBuilder().addEscalation(Exception.class, "handleFlowEscalation");
		this.constructFunction(work, "handleFlowEscalation").buildParameter();
		ReflectiveFunctionBuilder next = this.constructFunction(work, "next");
		next.buildObject("MO");
		next.getBuilder().addEscalation(Exception.class, "handleNextEscalation");
		this.constructFunction(work, "handleNextEscalation").buildParameter();

		// Bind the managed object
		switch (scope) {
		case FUNCTION:
			flow.getBuilder().addManagedObject("MO", "MO");
			next.getBuilder().addManagedObject("MO", "MO");
			break;

		case THREAD:
			this.getOfficeBuilder().addThreadManagedObject("MO", "MO");
			break;

		case PROCESS:
			this.getOfficeBuilder().addProcessManagedObject("MO", "MO");
			break;

		default:
			fail("Unknown managed object scope " + scope);
		}

		// Invoke the function
		this.invokeFunction("task", null);

		// Validate appropriate logic
		assertTrue("Task should be invoked", work.isTaskInvoked);
		assertNull("Should not obtain flow object", work.flowObject);
		assertSame("Should handle failure in sourcing managed object", escalation, work.flowEscalation);

		// Determine if same object
		boolean isSameManagedObject = false;
		if (scope == ManagedObjectScope.PROCESS) {
			isSameManagedObject = true;
		} else if ((scope == ManagedObjectScope.THREAD) && (!isSpawnThreadState)) {
			isSameManagedObject = true;
		}

		if (isSameManagedObject) {
			// Same managed object for next function (propagate failure)
			assertNull("Should not source managed object", work.nextObject);
			assertSame("Should be failure in sourcing managed object", work.nextEscalation);
		} else {
			// New managed object for next function (so sourced)
			assertSame("Should now source managed object", this.lifeCycle, work.nextObject);
			assertNull("Should be no failure in sourcing managed object", work.nextEscalation);
		}
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public boolean isTaskInvoked = false;

		public LifeCycleObject flowObject = null;

		public Throwable flowEscalation = null;

		public LifeCycleObject nextObject = null;

		public Throwable nextEscalation = null;

		public void task(ReflectiveFlow flow) {
			this.isTaskInvoked = true;
			flow.doFlow(null, null);
		}

		public void flow(LifeCycleObject object) {
			this.flowObject = object;
		}

		public void handleFlowEscalation(Throwable escalation) {
			this.flowEscalation = escalation;

			// Clear source escalation to source next object
			ManagedObjectFailedSourceHandleTest.this.lifeCycle.sourceFailure = null;
		}

		public void next(LifeCycleObject object) {
			this.nextObject = object;
		}

		public void handleNextEscalation(Throwable escalation) {
			this.nextEscalation = escalation;
		}
	}

}