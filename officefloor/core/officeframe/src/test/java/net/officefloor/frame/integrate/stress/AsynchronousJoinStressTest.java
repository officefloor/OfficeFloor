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

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Tests invoking asynchronous {@link Flow} instances and joining on the
 * resulting {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousJoinStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures no issues arising in stress asynchronous joins with a
	 * {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressAsynchronousJoin_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", MockTeamSource
				.createTeamIdentifier(), 100));
	}

	/**
	 * Ensures no issues arising in stress asynchronous joins with a
	 * {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressAsynchronousJoin_LeaderFollowerTeam()
			throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", MockTeamSource
				.createTeamIdentifier(), 5, 100));
	}

	/**
	 * Ensures no issues arising in stress asynchronous joins with a
	 * {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressAsynchronousJoin_ExecutorFixedTeam()
			throws Exception {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST",
				MockTeamSource.createTeamIdentifier(), 5));
	}

	/**
	 * Does the asynchronous join stress test.
	 * 
	 * @param team
	 *            {@link Team} to use to run the {@link Task} instances.
	 */
	private void doTest(Team team) throws Exception {

		final int MAX_ASYNCHRONOUS_FLOWS = 10000;
		final int MAX_ITERATIONS = 10;
		final int MAX_WAIT_TIME = 100;
		final long JOIN_TIME = MAX_WAIT_TIME * 1000; // 100 seconds
		this.setVerbose(true);

		// Construct the team
		this.constructTeam("TEAM", team);

		// Construct the work
		Tasks tasks = new Tasks(MAX_ASYNCHRONOUS_FLOWS, JOIN_TIME,
				MAX_ITERATIONS);
		ReflectiveWorkBuilder work = this.constructWork(tasks, "WORK",
				"invokeAndJoin");

		// Construct the invokeAndJoin task
		ReflectiveTaskBuilder invokeAndJoin = work.buildTask("invokeAndJoin",
				"TEAM");
		invokeAndJoin.buildTaskContext();
		invokeAndJoin.buildFlow("asynchronousTask",
				FlowInstigationStrategyEnum.ASYNCHRONOUS, null);

		// Construct the asynchronousTask task
		ReflectiveTaskBuilder asynchronousTask = work.buildTask(
				"asynchronousTask", "TEAM");
		asynchronousTask.buildTaskContext();

		// Invoke the work
		this.invokeWork("WORK", null, MAX_WAIT_TIME);

		// Verify all iterations completed
		assertEquals("Incorrect number of iterations", MAX_ITERATIONS,
				tasks.iterationCount);
	}

	/**
	 * Contains the {@link Task} functionality for testing.
	 */
	public class Tasks {

		/**
		 * Number of asynchronous {@link Flow} instances to invoke and
		 * join on per iteration.
		 */
		private final int maxAsynchronousFlows;

		/**
		 * Timeout for the joins.
		 */
		private final long joinTimeout;

		/**
		 * Maximum number of iterations to make.
		 */
		private final int maxIterations;

		/**
		 * Previous number of {@link FlowFuture} instances complete.
		 */
		private int previousFlowFuturesComplete = -1;

		/**
		 * {@link FlowFuture} instances.
		 */
		private FlowFuture[] flowFutures;

		/**
		 * Number of asynchronous {@link Task} instances run.
		 */
		private int asynchronousTasksRunCount;

		/**
		 * Number of iterations so far.
		 */
		public int iterationCount = 0;

		/**
		 * Initiate.
		 * 
		 * @param maxAsynchronousFlows
		 *            Number of asynchronous {@link Flow} instances to
		 *            invoke and join on per iteration.
		 * @param joinTimeout
		 *            Timeout of the joins.
		 * @param maxIterations
		 *            Maximum number of iterations to make.
		 */
		public Tasks(int maxAsynchronousFlows, long joinTimeout,
				int maxIterations) {
			this.maxAsynchronousFlows = maxAsynchronousFlows;
			this.joinTimeout = joinTimeout;
			this.maxIterations = maxIterations;

			// Set tasks run to max for first iteration to start invoking flows
			this.asynchronousTasksRunCount = this.maxAsynchronousFlows;
		}

		/**
		 * Invokes and joins the asynchronous {@link Flow} instances.
		 * 
		 * @param taskContext
		 *            {@link TaskContext}.
		 * @param flow
		 *            asynchronousTask {@link Flow}.
		 */
		public void invokeAndJoin(TaskContext<?, ?, ?> taskContext,
				ReflectiveFlow flow) {
			try {

				// Determine the number of flow futures complete
				int flowFuturesComplete = 0;
				if (this.flowFutures == null) {
					// First iteration, so flag to go to iterating
					flowFuturesComplete = this.maxAsynchronousFlows;
				} else {
					for (int i = 0; i < this.flowFutures.length; i++) {
						if (this.flowFutures[i].isComplete()) {
							flowFuturesComplete++;
						}
					}
				}

				// Fail if less complete than last time this task was run
				// Note: it is possible to be the same as completion of the
				// thread
				// occurs before activation of jobs. Steps would be:
				// 1) Thread A invoked and joined on
				// 2) Thread B invoked and joined on
				// 3) Threads A and B complete (isComplete returns true for
				// both)
				// 4) Thread A activates this task as it is complete
				// 5) This task runs and counts both Thread A and B complete
				// 6) Thread B activates this task as it is also complete
				// 7) This task runs and again counts both Thread A and B
				// complete
				assertTrue(
						"No less flows expected to be complete (previous="
								+ this.previousFlowFuturesComplete
								+ ", current=" + flowFuturesComplete + ")",
						(flowFuturesComplete >= this.previousFlowFuturesComplete));

				// Do not continue if all flows not yet complete
				if (flowFuturesComplete < this.maxAsynchronousFlows) {
					// Set 'new' previous flows complete and repeat this task
					this.previousFlowFuturesComplete = flowFuturesComplete;
					taskContext.setComplete(false);
					return;
				}

				// Safe as all threads are of same process
				synchronized (taskContext.getProcessLock()) {
					// Ensure all asynchronous tasks run
					assertEquals(
							"Incorrect number of asynchronous tasks run before re-run of job",
							this.maxAsynchronousFlows,
							this.asynchronousTasksRunCount);

					// Reset run count
					this.asynchronousTasksRunCount = 0;
				}

				// Increment the number of iterations
				this.iterationCount++;

				// Invoke the flows and join on them (no previous flows
				// complete)
				this.previousFlowFuturesComplete = -1;
				this.flowFutures = new FlowFuture[this.maxAsynchronousFlows];
				for (int i = 0; i < this.flowFutures.length; i++) {
					this.flowFutures[i] = flow.doFlow(null);
					taskContext.join(this.flowFutures[i], this.joinTimeout,
							null);
				}

				// Provide progress on number of iterations
				long asynchronousTasksRun = (this.iterationCount * this.maxAsynchronousFlows);
				if ((asynchronousTasksRun % (this.maxIterations / 10)) == 0) {
					AsynchronousJoinStressTest.this.printMessage("Iterations="
							+ this.iterationCount + " (asynchronous tasks="
							+ asynchronousTasksRun + ")");
				}

				// Determine if to stop iterating
				if (this.iterationCount < this.maxIterations) {
					// Continue as more iterations
					taskContext.setComplete(false);
				} else {
					// Provide indication that iterations complete
					AsynchronousJoinStressTest.this
							.printMessage("Iterations complete");
				}

			} catch (Throwable ex) {
				// Report the failure in the test
				System.err.println("FAILURE in test: "
						+ AsynchronousJoinStressTest.this.getName());
				ex.printStackTrace();

				// Propagate the failure
				fail(ex);
			}
		}

		/**
		 * Asynchronous {@link Task} invoked.
		 * 
		 * @param taskContext
		 *            {@link TaskContext}.
		 */
		public void asynchronousTask(TaskContext<?, ?, ?> taskContext)
				throws Exception {
			// Increment runs (safe as all threads of same process)
			synchronized (taskContext.getProcessLock()) {
				this.asynchronousTasksRunCount++;
			}
		}
	}
}