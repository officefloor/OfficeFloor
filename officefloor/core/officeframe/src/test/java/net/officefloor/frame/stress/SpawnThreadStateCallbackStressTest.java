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
package net.officefloor.frame.stress;

import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests invoking asynchronous {@link Flow} instances and joining on the
 * resulting {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpawnThreadStateCallbackStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures no issues arising in stress asynchronous joins with a
	 * {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressAsynchronousJoin_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", 100));
	}

	/**
	 * Ensures no issues arising in stress asynchronous joins with a
	 * {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressAsynchronousJoin_LeaderFollowerTeam() throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", MockTeamSource.createTeamIdentifier(), 5, 100));
	}

	/**
	 * Ensures no issues arising in stress asynchronous joins with a
	 * {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressAsynchronousJoin_ExecutorFixedTeam() throws Exception {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST", MockTeamSource.createTeamIdentifier(), 5));
	}

	/**
	 * Does the asynchronous join stress test.
	 * 
	 * @param team
	 *            {@link Team} to use to run the {@link ManagedFunction}
	 *            instances.
	 */
	private void doTest(Team team) throws Exception {
		
		fail("TODO fix hanging of test");

		final int MAX_ASYNCHRONOUS_FLOWS = 10000;
		final int MAX_ITERATIONS = 10;
		final int MAX_WAIT_TIME = 100;

		// Construct the team
		this.constructTeam("TEAM", team);

		// Construct the work
		Tasks tasks = new Tasks(MAX_ASYNCHRONOUS_FLOWS, MAX_ITERATIONS);

		// Construct the invokeAndJoin task
		ReflectiveFunctionBuilder invokeWithCallback = this.constructFunction(tasks, "invokeWithCallback");
		invokeWithCallback.getBuilder().setTeam("TEAM");
		invokeWithCallback.buildFlow("asynchronousTask", null, true);
		invokeWithCallback.buildFlow("invokeWithCallback", null, false);

		// Construct the asynchronousTask task
		ReflectiveFunctionBuilder asynchronousTask = this.constructFunction(tasks, "asynchronousTask");
		asynchronousTask.getBuilder().setTeam("TEAM");

		// Invoke the function
		this.invokeFunction("invokeWithCallback", null, MAX_WAIT_TIME);

		// Verify all iterations completed
		assertEquals("Incorrect number of iterations", MAX_ITERATIONS, tasks.callbackCount.get());
	}

	/**
	 * Contains the {@link ManagedFunction} functionality for testing.
	 */
	public class Tasks {

		/**
		 * Number of asynchronous {@link Flow} instances to invoke.
		 */
		private final int maxAsynchronousFlows;

		/**
		 * Maximum number of iterations to make.
		 */
		private final int maxIterations;

		/**
		 * Number of iterations undertaken.
		 */
		private final AtomicInteger iterationCount = new AtomicInteger(0);

		/**
		 * Number of asynchronous {@link ManagedFunction} instances run.
		 */
		private final AtomicInteger callbackCount = new AtomicInteger(0);

		/**
		 * Initiate.
		 * 
		 * @param maxAsynchronousFlows
		 *            Number of asynchronous {@link Flow} instances to invoke
		 *            and join on per iteration.
		 * @param maxIterations
		 *            Maximum number of iterations to make.
		 */
		public Tasks(int maxAsynchronousFlows, int maxIterations) {
			this.maxAsynchronousFlows = maxAsynchronousFlows;
			this.maxIterations = maxIterations;
		}

		/**
		 * Invokes and joins the asynchronous {@link Flow} instances.
		 * 
		 * @param flow
		 *            asynchronousTask {@link Flow}.
		 * @param repeat
		 *            {@link ReflectiveFlow} to repeat.
		 */
		public void invokeWithCallback(ReflectiveFlow flow, ReflectiveFlow repeat) {

			// Invoke the functions
			for (int i = 0; i < this.maxAsynchronousFlows; i++) {
				flow.doFlow(null, (escalation) -> {

					// Increment the number of callbacks
					int callbacks = this.callbackCount.incrementAndGet();

					// Determine if complete for callbacks
					if (callbacks >= this.maxAsynchronousFlows) {

						// Complete, determine if another iteration
						int iterations = this.iterationCount.incrementAndGet();
						if (iterations < this.maxIterations) {
							repeat.doFlow(null, null);
						}
					}
				});
			}
		}

		/**
		 * Asynchronous {@link ManagedFunction} invoked.
		 */
		public void asynchronousTask() {
		}
	}
}