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

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests running the same {@link ManagedFunction} many times.
 * 
 * @author Daniel Sagenschneider
 */
public class RepeatTaskStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures no issues arising in stress repeating a {@link ManagedFunction}
	 * with a {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressRepeat_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", 100));
	}

	/**
	 * Ensures no issues arising in stress repeating a {@link ManagedFunction}
	 * with a {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressRepeat_LeaderFollowerTeam() throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", 5, 100));
	}

	/**
	 * Ensures no issues arising in stress repeating a {@link ManagedFunction}
	 * with a {@link PassiveTeam}.
	 */
	@StressTest
	public void test_StressRepeat_PassiveTeam() throws Exception {
		this.doTest(new PassiveTeam());
	}

	/**
	 * Ensures no issues arising in stress repeating a {@link ManagedFunction}
	 * with a {@link ExecutorCachedTeamSource}.
	 */
	@StressTest
	public void test_StressRepeat_ExecutorCachedTeam() throws Exception {
		this.doTest(new ExecutorCachedTeamSource().createTeam());
	}

	/**
	 * Ensures no issues arising in stress repeating a {@link ManagedFunction}
	 * with a {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressRepeat_ExecutorFixedTeam() throws Exception {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST", 5));
	}

	/**
	 * Does the repeat stress test.
	 * 
	 * @param team
	 *            {@link Team} to use to run the {@link ManagedFunction}
	 *            instances.
	 */
	private void doTest(Team team) throws Exception {

		int REPEAT_COUNT = 1000000;
		int MAX_RUN_TIME = 100;
		this.setVerbose(true);

		// Create the repeat
		RepeatTask repeat = new RepeatTask(REPEAT_COUNT);

		// Register the repeat task
		ReflectiveFunctionBuilder function = this.constructFunction(repeat, "repeat");
		function.getBuilder().setResponsibleTeam("TEAM");
		function.buildFlow("repeat", null, false);
		this.constructTeam("TEAM", team);

		// Run the repeats
		this.invokeFunction("repeat", null, MAX_RUN_TIME);

		// Ensure correct number of repeats
		assertEquals("Incorrect number of repeats", REPEAT_COUNT, repeat.getRepeatCount());
	}

	/**
	 * Functionality.
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
		 * @param repeat
		 *            {@link ReflectiveFlow} to repeat.
		 */
		public synchronized void repeat(ReflectiveFlow repeat) {

			// Determine if repeated enough
			if (this.repeatCount >= this.maxRepeatCalls) {
				return; // repeated enough
			}

			// Set up to repeat again
			this.repeatCount++;
			repeat.doFlow(null, null);

			// Output progress
			if ((this.repeatCount % 1000000) == 0) {
				RepeatTaskStressTest.this.printMessage("Repeat Calls=" + this.repeatCount);
			}
		}
	}

}