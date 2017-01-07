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
package net.officefloor.frame.impl.execute.managedobject.asynchronous;

import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.StepTeam;

/**
 * Tests loading the {@link ManagedObject} asynchronously.
 * 
 * @author Daniel Sagenschneider
 */
public class AsyncManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link TestWork}.
	 */
	private TestWork work;

	/**
	 * {@link StepTeam}.
	 */
	private StepTeam team;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Obtain the office name
		String officeName = this.getOfficeName();

		// Construct the managed object
		ManagedObjectBuilder<None> moBuilder = this.constructManagedObject("MO", TestManagedObjectSource.class,
				officeName);
		moBuilder.setTimeout(1000);

		// Construct the function to execute
		this.work = new TestWork();
		ReflectiveFunctionBuilder functionBuilder = this.constructFunction(this.work, "task");
		functionBuilder.getBuilder().setTeam("TEAM");
		functionBuilder.buildObject("MO", ManagedObjectScope.FUNCTION);
		functionBuilder.buildFlow("flow", null, false);
		this.constructFunction(this.work, "flow");

		// Construct team to run function
		this.team = new StepTeam();
		this.constructTeam("TEAM", this.team);

		// Open the OfficeFloor and invoke function
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
		officeFloor.getOffice(officeName).getFunctionManager("task").invokeProcess(null, null);
	}

	/**
	 * Ensures that able to load {@link ManagedObject} asynchronously.
	 */
	public void testAsynchronousSourceManagedObject() throws Exception {

		// Execute Job, attempting to load managed object
		this.team.executeJob();
		assertFalse("Should not run function", this.work.isFunctionExecuted);

		// Load managed object later, waiting for activation by OfficeManager
		TestManagedObjectSource.loadManagedObject(new TestManagedObject());

		// Managed Object available, so should complete on next execution
		this.team.executeJob();
		assertTrue("Function now run after object available", this.work.isFunctionExecuted);
	}

	/**
	 * Ensures able to handle asynchronous operations by the
	 * {@link ManagedObject}.
	 */
	public void testAsynchronousOperation() throws Exception {

		final TestManagedObject mo = new TestManagedObject();

		// Load managed object and trigger asynchronous operation
		TestManagedObjectSource.loadManagedObject(mo);
		this.work.isTriggerAsynchronousOperation = true;
		this.team.executeJob();
		assertTrue("Function run as object available", this.work.isFunctionExecuted);
		assertFalse("Callback should not be executed as waiting on operation", this.work.isCallbackExecuted);

		// Notify asynchronous operation over, activate by OfficeManager
		mo.listener.notifyComplete();
		assertFalse("Callback should be queued to same team, so not exeuted", this.work.isCallbackExecuted);

		// Asynchronous operation over, so should complete on next execution
		this.team.executeJob();
		assertTrue("Callback run as asynchronous operation complete", this.work.isCallbackExecuted);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		/**
		 * Flag indicating if the {@link ManagedFunction} was executed.
		 */
		public boolean isFunctionExecuted = false;

		/**
		 * Flag indicating if the {@link FlowCallback} was executed.
		 */
		public boolean isCallbackExecuted = false;

		/**
		 * Flag indicating if the {@link Job} will trigger an asynchronous
		 * operation and not complete.
		 */
		public boolean isTriggerAsynchronousOperation = false;

		/**
		 * {@link Job} for execution.
		 * 
		 * @param object
		 *            Object from the {@link ManagedObject}.
		 * @param flow
		 *            {@link ReflectiveFlow}.
		 */
		public void task(Object object, ReflectiveFlow flow) {

			// Flag that executed
			this.isFunctionExecuted = true;

			// Determine if trigger an asynchronous operation
			if (this.isTriggerAsynchronousOperation) {
				// Trigger asynchronous operation
				TestManagedObject mo = (TestManagedObject) object;
				mo.listener.notifyStarted();

				// Clear trigger flag (as triggered)
				this.isTriggerAsynchronousOperation = false;

				// Undertake flow (will wait until operation complete)
				flow.doFlow(null, new FlowCallback() {
					@Override
					public void run(Throwable escalation) throws Throwable {
						TestWork.this.isCallbackExecuted = true;
					}
				});
			}
		}

		/**
		 * {@link Flow} to be invoked.
		 */
		public void flow() {
		}
	}

	/**
	 * Test {@link ManagedObjectSource}.
	 */
	@TestSource
	public static class TestManagedObjectSource extends AbstractAsyncManagedObjectSource<None, None> {

		/**
		 * {@link ManagedObjectUser}.
		 */
		private static ManagedObjectUser managedObjectUser = null;

		/**
		 * {@link ManagedObject} to be loaded immediately.
		 */
		private static ManagedObject managedObject = null;

		/**
		 * Loads the {@link ManagedObject}.
		 * 
		 * @param managedObject
		 *            {@link ManagedObject}.
		 */
		public static void loadManagedObject(ManagedObject managedObject) {
			if (managedObjectUser != null) {
				// Load managed object
				managedObjectUser.setManagedObject(managedObject);
			} else {
				// Store to source immediately
				TestManagedObjectSource.managedObject = managedObject;
			}
		}

		/**
		 * Initiate.
		 */
		public TestManagedObjectSource() {
			// Reset for next test
			managedObjectUser = null;
			managedObject = null;
		}

		/*
		 * ============== AbstractAsyncManagedObjectSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
			context.setManagedObjectClass(TestManagedObject.class);
			context.setObjectClass(Object.class);
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			// Determine if load immediately
			if (managedObject != null) {
				// Load immediately
				user.setManagedObject(managedObject);
			} else {
				// Store for later loading
				managedObjectUser = user;
			}
		}
	}

	/**
	 * Test {@link AsynchronousManagedObject}.
	 */
	private class TestManagedObject implements AsynchronousManagedObject {

		/**
		 * {@link AsynchronousListener}.
		 */
		public AsynchronousListener listener;

		/*
		 * ================= AsynchronousManagedObject =======================
		 */

		@Override
		public void registerAsynchronousListener(AsynchronousListener listener) {
			this.listener = listener;
		}

		@Override
		public Object getObject() throws Exception {
			return this;
		}
	}

}