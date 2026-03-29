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

package net.officefloor.frame.stress.object;

import junit.framework.TestSuite;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
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
	protected int getIterationCount() {
		return 100000;
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
				parameter.listener.complete(null);
			}

			// Determine if continue
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Trigger asynchronous operation
			managedObject.listener.start(null);

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

		private AsynchronousContext listener;

		@Override
		public void setAsynchronousContext(AsynchronousContext listener) {
			this.listener = listener;
		}

		@Override
		public Object getObject() {
			return this;
		}
	}

}
