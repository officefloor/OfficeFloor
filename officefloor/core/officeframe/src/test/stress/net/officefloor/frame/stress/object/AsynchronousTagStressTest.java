/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.frame.stress.object;

import junit.framework.TestSuite;
import net.officefloor.frame.api.managedobject.AsynchronousListener;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link AsynchronousManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousTagStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(AsynchronousTagStressTest.class);
	}

	@Override
	protected boolean isTestEachManagedObjectScope() {
		return true;
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct the managed objects
		this.constructManagedObject("ASYNCHRONOUS_ONE", null, () -> new Asynchronous()).setTimeout(1000);
		this.constructManagedObject("ASYNCHRONOUS_TWO", null, () -> new Asynchronous()).setTimeout(1000);

		// Construct the functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder taskOne = this.constructFunction(work, "taskOne");
		context.loadResponsibleTeam(taskOne.getBuilder());
		taskOne.buildParameter();
		taskOne.buildObject("ASYNCHRONOUS_ONE", context.getManagedObjectScope());
		taskOne.buildFlow("taskTwo", Asynchronous.class, false);
		ReflectiveFunctionBuilder taskTwo = this.constructFunction(work, "taskTwo");
		context.loadOtherTeam(taskTwo.getBuilder());
		taskTwo.buildParameter();
		taskTwo.buildObject("ASYNCHRONOUS_TWO", context.getManagedObjectScope());
		taskTwo.buildFlow("taskOne", Asynchronous.class, false);

		// Run
		context.setInitialFunction("taskOne", null);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		private final StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void taskOne(Asynchronous parameter, Asynchronous managedObject, ReflectiveFlow taskTwo) {

			// Notify complete for other task
			if (parameter != null) {
				parameter.listener.notifyComplete();
			}

			// Determine if continue
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Trigger asynchronous operation
			managedObject.listener.notifyStarted();

			// Call other task to complete operation
			taskTwo.doFlow(managedObject, null);
		}

		public void taskTwo(Asynchronous parameter, Asynchronous managedObject, ReflectiveFlow taskOne) {
			this.taskOne(parameter, managedObject, taskOne);
		}
	}

	/**
	 * {@link AsynchronousManagedObject}.
	 */
	private static class Asynchronous implements AsynchronousManagedObject {

		private AsynchronousListener listener;

		@Override
		public void registerAsynchronousListener(AsynchronousListener listener) {
			this.listener = listener;
		}

		@Override
		public Object getObject() {
			return this;
		}
	}

}