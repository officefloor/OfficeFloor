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

package net.officefloor.frame.stress.function;

import junit.framework.TestSuite;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests {@link AsynchronousFlow} for the {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousFlowMultipleStressTest extends AbstractStressTestCase {

	protected int getIterationCount() {
		return 1000;
	}

	public static TestSuite suite() {
		return createSuite(AsynchronousFlowMultipleStressTest.class);
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		// Create the asynchronous flow invoker
		AsynchronousFlowInvoker functionality = new AsynchronousFlowInvoker(context);

		// Register the functions
		ReflectiveFunctionBuilder trigger = this.constructFunction(functionality, "trigger");
		trigger.buildManagedFunctionContext();
		trigger.buildFlow("nextTask", null, false);
		context.loadResponsibleTeam(trigger.getBuilder());
		ReflectiveFunctionBuilder nextTask = this.constructFunction(functionality, "nextTask");
		context.loadOtherTeam(nextTask.getBuilder());

		// Run the repeats
		context.setInitialFunction("trigger", 1);
	}

	/**
	 * Test functionality.
	 */
	public class AsynchronousFlowInvoker {

		private final StressContext context;

		public AsynchronousFlowInvoker(StressContext context) {
			this.context = context;
		}

		public void trigger(ManagedFunctionContext<?, ?> mfContext, ReflectiveFlow nextTask) {

			// Trigger all flows in parallel
			for (int i = 0; i < this.context.getMaximumIterations(); i++) {

				// Undertake asynchronous flow
				AsynchronousFlow flow = mfContext.createAsynchronousFlow();
				mfContext.getExecutor().execute(() -> {
					flow.complete(() -> nextTask.doFlow(null, null));
				});
			}
		}

		public void nextTask() {
			this.context.incrementIterationAndIsComplete();
		}
	}

}
