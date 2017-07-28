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
package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.ReflectiveGovernanceBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure disregard {@link Governance} on {@link Escalation} handling.
 *
 * @author Daniel Sagenschneider
 */
public class DisregardGovernanceOnFailureTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can enforce {@link Governance} on a {@link ManagedObject} bound to
	 * {@link ProcessState}.
	 */
	public void testDisregardGovernanceByEscalationHandler_boundTo_ProcessState() throws Exception {
		this.doDisregardGovernanceOnFaillureTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure can enforce {@link Governance} on a {@link ManagedObject} bound to
	 * {@link ThreadState}.
	 */
	public void testDisregardGovernanceByEscalationHandler_boundTo_ThreadState() throws Exception {
		this.doDisregardGovernanceOnFaillureTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure can enforce {@link Governance} on a {@link ManagedObject} bound to
	 * {@link ManagedFunction}.
	 */
	public void testDisregardGovernanceByEscalationHandler_boundTo_FunctionState() throws Exception {
		this.doDisregardGovernanceOnFaillureTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure can enforce the {@link Governance}.
	 */
	public void doDisregardGovernanceOnFaillureTest(ManagedObjectScope scope) throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (context) -> context.addManagedObjectExtensionInterface(TestObject.class,
				(managedObject) -> (TestObject) managedObject);
		object.isRecycleFunction = true;

		// Construct the function
		TestWork work = new TestWork(new Exception("TEST"));
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.buildObject("MO", scope).mapGovernance("GOVERNANCE");
		task.getBuilder().addGovernance("GOVERNANCE");
		task.getBuilder().addEscalation(Exception.class, "handle");
		this.constructFunction(work, "handle").buildParameter();

		// Provide governance
		TestGovernance govern = new TestGovernance();
		ReflectiveGovernanceBuilder governance = this.constructGovernance(govern, "GOVERNANCE");
		governance.disregard("disregard");

		// Invoke the function (ensuring disregard)
		this.invokeFunctionAndValidate("task", null, "task", "disregard", "handle");

		// Ensure handle exception
		assertSame("Should have handled the exception", work.exception, work.handledException);

		// Ensure the managed object extension is disregarded
		assertEquals("Incorrect number of extensions", 1, govern.disregarded.length);
		assertSame("Incorrect extension", object, govern.disregarded[0]);

		// Ensure the managed object is also cleaned up
		assertSame("Managed object should be cleaned up", object, object.recycledManagedObject);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private final Exception exception;

		private TestObject object = null;

		private Exception handledException = null;

		public TestWork(Exception exception) {
			this.exception = exception;
		}

		public void task(TestObject object) throws Exception {
			assertNull("Managed object should not be cleaned up", object.recycledManagedObject);
			this.object = object;
			throw this.exception;
		}

		public void handle(Exception escalation) {
			assertNull("Managed object should not be cleaned up", this.object.recycledManagedObject);
			this.handledException = escalation;
		}
	}

	/**
	 * Test {@link Governance}.
	 */
	public class TestGovernance {

		private TestObject[] disregarded;

		public void disregard(TestObject[] extensions) {
			assertEquals("Incorrect number of extensions", 1, extensions.length);
			assertNull("Should not clean up managed object until after disregarding governance",
					extensions[0].recycledManagedObject);
			this.disregarded = extensions;
		}
	}

}
