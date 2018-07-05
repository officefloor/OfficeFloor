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
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure dynamically invoking {@link ManagedFunction} instances is stress
 * tested.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamicFlowStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(DynamicFlowStressTest.class);
	}

	@Override
	protected int getIterationCount() {
		return 1000000;
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Construct functions
		TestWork work = new TestWork(context);
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "trigger");
		trigger.buildManagedFunctionContext();
		trigger.buildParameter();
		context.loadOtherTeam(trigger.getBuilder());
		ReflectiveFunctionBuilder dynamic = this.constructFunction(work, "dynamic");
		dynamic.buildFlow("trigger", null, false);
		dynamic.buildParameter();
		context.loadResponsibleTeam(dynamic.getBuilder());

		// Trigger
		context.setInitialFunction("trigger", 1);
	}

	/**
	 * Test functionality
	 */
	public class TestWork {

		private final StressContext context;

		public TestWork(StressContext context) {
			this.context = context;
		}

		public void trigger(ManagedFunctionContext<?, ?> context, Integer iteration) throws Exception {
			// Invoke the dynamic flow
			context.doFlow("dynamic", iteration, null);
		}

		public void dynamic(ReflectiveFlow flow, Integer iteration) {

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete(iteration)) {
				return;
			}

			// Trigger again
			flow.doFlow(iteration.intValue() + 1, null);
		}
	}

}