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
package net.officefloor.frame.stress.function;

import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestSuite;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.stress.AbstractStressTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests invoking a parallel {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class ParallelFunctionStressTest extends AbstractStressTestCase {

	public static TestSuite suite() {
		return createSuite(ParallelFunctionStressTest.class);
	}

	@Override
	protected int getIterationCount() {
		return 10000;
	}

	@Override
	protected void constructTest(StressContext context) throws Exception {

		fail("TODO determine why hangs on completion");
		
		// Register the parallel tasks
		ParallelInvoker work = new ParallelInvoker(context);
		ReflectiveFunctionBuilder trigger = this.constructFunction(work, "trigger");
		trigger.buildFlow("parallel", Integer.class, false);
		trigger.buildFlow("trigger", null, false);
		context.loadOtherTeam(trigger.getBuilder());
		ReflectiveFunctionBuilder parallel = this.constructFunction(work, "parallel");
		parallel.buildFlow("parallel", Integer.class, false);
		context.loadResponsibleTeam(parallel.getBuilder());

		// Run the repeats
		context.setInitialFunction("trigger", null);

		// Ensure correct number of completions of parallel flows
		context.setValidation(() -> assertEquals("Incorrect number of parallel completions", this.getIterationCount(),
				work.completions.get()));
	}

	/**
	 * Functionality.
	 */
	public class ParallelInvoker {

		private final StressContext context;

		private final int maxFlowFunctions = 5;

		private final AtomicInteger flowFunctions = new AtomicInteger(0);

		private final AtomicInteger completions = new AtomicInteger(0);

		public ParallelInvoker(StressContext context) {
			this.context = context;
		}

		public void trigger(ReflectiveFlow flow, ReflectiveFlow repeat) {

			// Invoke the parallel task
			this.flowFunctions.set(0);
			flow.doFlow(new Integer(1), (escalation) -> {
				assertNull("Should be no failure", escalation);
				assertEquals("Incorrect number of flow functions", this.maxFlowFunctions, this.flowFunctions.get());
				this.completions.incrementAndGet();
			});

			// Determine if complete
			if (this.context.incrementIterationAndIsComplete()) {
				return;
			}

			// Repeat for another iteration
			repeat.doFlow(null, null);
		}

		public void parallel(ReflectiveFlow repeat) {

			// Determine if parallel flow complete
			int flowCount = this.flowFunctions.incrementAndGet();
			if (flowCount >= this.maxFlowFunctions) {
				return;
			}

			// Repeat task for parallel flow to continue
			repeat.doFlow(null, null);
		}
	}

}