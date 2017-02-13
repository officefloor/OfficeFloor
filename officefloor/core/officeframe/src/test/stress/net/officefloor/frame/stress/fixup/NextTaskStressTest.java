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
package net.officefloor.frame.stress.fixup;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests invoking a next {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class NextTaskStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures no issues arising in stress next {@link ManagedFunction} with a
	 * {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressNextTask_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", 100));
	}

	/**
	 * Ensures no issues arising in stress next {@link ManagedFunction} with a
	 * {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressNextTask_LeaderFollowerTeam() throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", 5, 100));
	}

	/**
	 * Ensures no issues arising in stress next {@link ManagedFunction} with a
	 * {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressNextTask_ExecutorFixedTeam() throws Exception {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST", 5));
	}

	/**
	 * Ensures no issues arising in stress next {@link ManagedFunction} with a
	 * {@link PassiveTeam}.
	 */
	@StressTest
	public void test_StressNextTask_PassiveTeam() throws Exception {
		this.doTest(new PassiveTeam());
	}

	/**
	 * Ensures no issues arising in stress next {@link ManagedFunction} with no
	 * {@link Team}.
	 */
	@StressTest
	public void test_StressNextTask_NoTeam() throws Exception {
		this.doTest((Team) null);
	}

	/**
	 * Does the parallel stress test.
	 * 
	 * @param team
	 *            {@link Team} to use to run the {@link ManagedFunction}
	 *            instances.
	 */
	private void doTest(Team team) throws Exception {

		int NEXT_TASK_COUNT = 1000000;
		int MAX_RUN_TIME = 100;
		this.setVerbose(true);

		// Create the next task invoker
		NextTaskInvoker functionality = new NextTaskInvoker(NEXT_TASK_COUNT);

		// Construct the team
		String teamName = null;
		if (team != null) {
			teamName = "TEAM";
			this.constructTeam(teamName, team);
		}

		// Register the next tasks
		ReflectiveFunctionBuilder trigger = this.constructFunction(functionality, "trigger");
		trigger.getBuilder().setResponsibleTeam(teamName);
		trigger.buildParameter();
		trigger.setNextFunction("nextTask");
		ReflectiveFunctionBuilder nextTask = this.constructFunction(functionality, "nextTask");
		nextTask.getBuilder().setResponsibleTeam(teamName);
		nextTask.buildParameter();
		nextTask.buildFlow("trigger", Integer.class, false);

		// Run the repeats
		this.invokeFunction("trigger", new Integer(1), MAX_RUN_TIME);

		// Ensure correct number of repeats
		synchronized (functionality) {
			assertEquals("Incorrect number of next tasks run", NEXT_TASK_COUNT, functionality.nextTaskCount);
		}
	}

	/**
	 * Functionality.
	 */
	public class NextTaskInvoker {

		/**
		 * Maximum number of next tasks.
		 */
		private final int maxNextTasks;

		/**
		 * Number of next {@link ManagedFunction} instances run.
		 */
		public long nextTaskCount = 0;

		/**
		 * Initiate.
		 * 
		 * @param maxNextTasks
		 *            Maximum number of parallel calls.
		 */
		public NextTaskInvoker(int maxNextTasks) {
			this.maxNextTasks = maxNextTasks;
		}

		/**
		 * Allows for moving onto the next {@link ManagedFunction}.
		 * 
		 * @param count
		 *            Count to pass to next {@link ManagedFunction}.
		 */
		public Integer trigger(Integer count) {
			return count;
		}

		/**
		 * Next {@link ManagedFunction}.
		 * 
		 * @param callCount
		 *            Number of next {@link ManagedFunction} instances invoked
		 *            so far.
		 * @param flow
		 *            Trigger {@link ManagedFunction}.
		 */
		public void nextTask(Integer callCount, ReflectiveFlow flow) {

			// Increment the next task calls
			long count;
			synchronized (this) {
				this.nextTaskCount++;
				count = this.nextTaskCount;
			}

			// Output progress
			if ((count % 1000000) == 0) {
				NextTaskStressTest.this.printMessage("Next tasks called=" + count);
			}

			// Determine if max next task calls made
			if (callCount.intValue() >= this.maxNextTasks) {
				// No further next task calls
				return;
			}

			// Trigger for another next task
			flow.doFlow(new Integer(callCount.intValue() + 1), null);
		}
	}

}