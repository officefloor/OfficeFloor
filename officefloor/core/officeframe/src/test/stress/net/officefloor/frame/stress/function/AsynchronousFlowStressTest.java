/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.stress.function;

import junit.framework.TestSuite;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests {@link AsynchronousFlow} for the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousFlowStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(AsynchronousFlowStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Create the object
		TestObject object = new TestObject();
		this.constructManagedObject(object, "MO", this.getOfficeName());

		// Create the asynchronous flow invoker
		AsynchronousFlowInvoker functionality = new AsynchronousFlowInvoker(context);

		// Register the functions
		ReflectiveFunctionBuilder trigger = this.constructFunction(functionality, "trigger");
		trigger.buildObject("MO", ManagedObjectScope.THREAD);
		trigger.buildManagedFunctionContext();
		trigger.setNextFunction("nextTask");
		context.loadOtherTeam(trigger.getBuilder());
		ReflectiveFunctionBuilder nextTask = this.constructFunction(functionality, "nextTask");
		nextTask.buildObject("MO");
		nextTask.buildFlow("trigger", Integer.class, false);
		context.loadResponsibleTeam(nextTask.getBuilder());

		// Run the repeats
		context.setInitialFunction("trigger", 1);
	}

	public class TestObject {
		private String value = "trigger";
	}

	/**
	 * Test functionality.
	 */
	public class AsynchronousFlowInvoker {

		private final StressContext context;

		public AsynchronousFlowInvoker(StressContext context) {
			this.context = context;
		}

		public void trigger(TestObject object, ManagedFunctionContext<?, ?> context) {

			// Ensure appropriate initial state
			assertEquals("Incorrect initial state", "trigger", object.value);

			// Undertake asynchronous flow
			AsynchronousFlow flow = context.createAsynchronousFlow();
			context.getExecutor().execute(() -> {
				flow.complete(() -> object.value = "nextTask");
			});
		}

		public void nextTask(TestObject object, ReflectiveFlow flow) {

			// Determine if asynchronous flow updated state
			assertEquals("Incorrect async flow state", "nextTask", object.value);

			// Determine if complete
			if (context.incrementIterationAndIsComplete()) {
				return;
			}

			// Trigger for another
			object.value = "trigger";
			flow.doFlow(null, null);
		}
	}

}