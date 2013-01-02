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
package net.officefloor.frame.integrate.stress;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Stress tests invoking a parallel {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public class ParallelTaskStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures no issues arising in stress parallel calls with a
	 * {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressParallel_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", MockTeamSource
				.createTeamIdentifier(), 100));
	}

	/**
	 * Ensures no issues arising in stress parallel calls with a
	 * {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressParallel_LeaderFollowerTeam() throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", MockTeamSource
				.createTeamIdentifier(), 5, 100));
	}

	/**
	 * Ensures no issues arising in stress parallel calls with a
	 * {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressParallel_ExecutorFixedTeam() throws Exception {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST",
				MockTeamSource.createTeamIdentifier(), 5));
	}

	/**
	 * Does the parallel stress test.
	 * 
	 * @param team
	 *            {@link Team} to use to run the {@link Task} instances.
	 */
	private void doTest(Team team) throws Exception {

		int TRIGGER_COUNT = 100;
		int PARALLEL_COUNT = 10000;
		int MAX_RUN_TIME = 100;
		this.setVerbose(true);

		// Create the parallel invoker
		ParallelInvoker functionality = new ParallelInvoker(PARALLEL_COUNT,
				TRIGGER_COUNT);

		// Register the parallel tasks
		ReflectiveWorkBuilder work = this.constructWork(functionality, "work",
				"trigger");
		ReflectiveTaskBuilder trigger = work.buildTask("trigger", "TEAM");
		trigger.buildTaskContext();
		trigger.buildFlow("parallel", FlowInstigationStrategyEnum.PARALLEL,
				Integer.class);
		ReflectiveTaskBuilder parallel = work.buildTask("parallel", "TEAM");
		parallel.buildParameter();
		parallel.buildFlow("parallel", FlowInstigationStrategyEnum.PARALLEL,
				Integer.class);
		this.constructTeam("TEAM", team);

		// Run the repeats
		this.invokeWork("work", null, MAX_RUN_TIME);

		// Ensure correct number of repeats
		synchronized (functionality) {
			assertEquals("Incorrect number of parallel tasks run",
					(TRIGGER_COUNT * PARALLEL_COUNT),
					functionality.parallelCount);
		}
	}

	/**
	 * {@link Work}.
	 */
	public class ParallelInvoker {

		/**
		 * Maximum number of parallel calls.
		 */
		private final int maxParallelCalls;

		/**
		 * Number of parallel {@link Task} instances run.
		 */
		public long parallelCount = 0;

		/**
		 * Maximum number of times to trigger parallel {@link Task} instances.
		 */
		private final int maxTriggers;

		/**
		 * Number of times parallel calls triggered.
		 */
		private int triggerCount = 0;

		/**
		 * Initiate.
		 * 
		 * @param maxParallelCalls
		 *            Maximum number of parallel calls.
		 * @param maxTriggers
		 *            Maximum number of times to trigger parallel {@link Task}
		 *            instances.
		 */
		public ParallelInvoker(int maxParallelCalls, int maxTriggers) {
			this.maxParallelCalls = maxParallelCalls;
			this.maxTriggers = maxTriggers;
		}

		/**
		 * Triggers the parallel flow and repeats so many times.
		 * 
		 * @param taskContext
		 *            {@link TaskContext}.
		 * @param flow
		 *            Parallel {@link Task}.
		 */
		public void trigger(TaskContext<?, ?, ?> taskContext,
				ReflectiveFlow flow) {

			// Invoke the parallel task
			flow.doFlow(new Integer(1));

			// Determine if to stop triggering parallel tasks
			this.triggerCount++;
			if (this.triggerCount < this.maxTriggers) {
				taskContext.setComplete(false);
			}
		}

		/**
		 * Parallel {@link Task}.
		 * 
		 * @param callCount
		 *            Number of parallel calls so far.
		 * @param flow
		 *            Parallel {@link Task}.
		 */
		public void parallel(Integer callCount, ReflectiveFlow flow) {

			// Increment the parallel calls
			long count;
			synchronized (this) {
				this.parallelCount++;
				count = this.parallelCount;
			}

			// Output progress
			if ((count % 1000000) == 0) {
				ParallelTaskStressTest.this
						.printMessage("Parallel tasks called=" + count);
			}

			// Determine if max parallel calls made
			if (callCount.intValue() >= this.maxParallelCalls) {
				// No further parallel calls
				return;
			}

			// Invoke another parallel task
			flow.doFlow(new Integer(callCount.intValue() + 1));
		}
	}

}