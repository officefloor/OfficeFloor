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
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;

/**
 * Tests running the same {@link Task} many times.
 * 
 * @author Daniel Sagenschneider
 */
public class RepeatTaskStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures no issues arising in stress repeating a {@link Task} with a
	 * {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressRepeat_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", MockTeamSource.createTeamIdentifier(), 100));
	}

	/**
	 * Ensures no issues arising in stress repeating a {@link Task} with a
	 * {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressRepeat_LeaderFollowerTeam() throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", MockTeamSource.createTeamIdentifier(), 5,
				100));
	}

	/**
	 * Ensures no issues arising in stress repeating a {@link Task} with a
	 * {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressRepeat_ExecutorFixedTeam() throws Exception {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST",
				MockTeamSource.createTeamIdentifier(), 5));
	}

	/**
	 * Does the repeat stress test.
	 * 
	 * @param team
	 *            {@link Team} to use to run the {@link Task} instances.
	 */
	private void doTest(Team team) throws Exception {

		int REPEAT_COUNT = 1000000;
		int MAX_RUN_TIME = 100;
		this.setVerbose(true);

		// Create the repeat
		RepeatTask repeat = new RepeatTask(REPEAT_COUNT);

		// Register the repeat task
		this.constructWork(repeat, "work", "repeat")
				.buildTask("repeat", "TEAM").buildTaskContext();
		this.constructTeam("TEAM", team);

		// Run the repeats
		this.invokeWork("work", null, MAX_RUN_TIME);

		// Ensure correct number of repeats
		assertEquals("Incorrect number of repeats", REPEAT_COUNT,
				repeat.getRepeatCount());
	}

	/**
	 * {@link Work}.
	 */
	public class RepeatTask {

		/**
		 * Number of times to repeat.
		 */
		private final int maxRepeatCalls;

		/**
		 * Number of times repeat called.
		 */
		private int repeatCount;

		/**
		 * Initiate.
		 * 
		 * @param maxRepeatCalls
		 *            Number of times to repeat.
		 */
		public RepeatTask(int maxRepeatCalls) {
			this.maxRepeatCalls = maxRepeatCalls;
		}

		/**
		 * Obtains the number of times repeated.
		 */
		public synchronized int getRepeatCount() {
			return this.repeatCount;
		}

		/**
		 * Repeating task.
		 * 
		 * @param context
		 *            {@link TaskContext}.
		 */
		public synchronized void repeat(TaskContext<?, ?, ?> context) {

			// Determine if repeated enough
			if (this.repeatCount >= this.maxRepeatCalls) {
				return; // repeated enough
			}

			// Set up to repeat again
			this.repeatCount++;
			context.setComplete(false);

			// Output progress
			if ((this.repeatCount % 1000000) == 0) {
				RepeatTaskStressTest.this.printMessage("Repeat Calls="
						+ this.repeatCount);
			}
		}
	}

}