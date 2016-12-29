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

import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.impl.spi.team.ExecutorFixedTeamSource;
import net.officefloor.frame.impl.spi.team.LeaderFollowerTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests invoking sequential {@link Flow} many times.
 * 
 * @author Daniel Sagenschneider
 */
public class SequentialTaskStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Stress tests with the {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressSequential_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", 100));
	}

	/**
	 * Stress tests with the {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressSequential_LeaderFollowerTeam() throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", MockTeamSource.createTeamIdentifier(), 5, 100));
	}

	/**
	 * Stress tests with the {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressSequential_ExecutorFixedTeam() throws Exception {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST", MockTeamSource.createTeamIdentifier(), 5));
	}

	/**
	 * Stress tests with the {@link ExecutorCacheTeamSource}.
	 */
	@StressTest
	public void test_StressSequential_ExecutorCachedTeam() throws Exception {
		this.doTest(new ExecutorCachedTeamSource().createTeam());
	}

	/**
	 * Stress tests with the {@link PassiveTeam}.
	 */
	@StressTest
	public void test_StressSequential_PassiveTeam() throws Exception {
		this.doTest(new PassiveTeam());
	}

	/**
	 * Does the sequential call stress test with the {@link Team}.
	 * 
	 * @param team
	 *            {@link Team}.
	 */
	public void doTest(Team team) throws Exception {

		int SEQUENTIAL_COUNT = 1000000;
		int MAX_RUN_TIME = 100;
		this.setVerbose(true);

		// Create the sequential
		SequentialInvokeTask sequential = new SequentialInvokeTask(SEQUENTIAL_COUNT);

		// Register the sequential function
		ReflectiveFunctionBuilder function = this.constructFunction(sequential, "sequential", "TEAM");
		function.buildParameter();
		function.buildFlow("sequential", Integer.class, false);
		this.constructTeam("TEAM", team);

		// Run the repeats
		this.invokeFunction("sequential", new Integer(1), MAX_RUN_TIME);

		// Ensure is complete
		assertEquals("Did not complete all sequential calls", SEQUENTIAL_COUNT, sequential.sequentialCallCount.get());
	}

	/**
	 * Functionality for test.
	 */
	public class SequentialInvokeTask {

		/**
		 * Number of times to make a sequential call.
		 */
		private final int maxSequentialCalls;

		/**
		 * Number of sequential calls made.
		 */
		public AtomicInteger sequentialCallCount = new AtomicInteger(0);

		/**
		 * Initiate.
		 * 
		 * @param maxSequentialCalls
		 *            Number of times to make a sequential call.
		 */
		public SequentialInvokeTask(int maxSequentialCalls) {
			this.maxSequentialCalls = maxSequentialCalls;
		}

		/**
		 * Sequential invoke task.
		 * 
		 * @param callCount
		 *            Number of sequential calls so far.
		 * @param flow
		 *            {@link ReflectiveFlow} to invoke the sequential
		 *            {@link Flow}.
		 */
		public void sequential(Integer callCount, ReflectiveFlow flow) {

			// Indicate the number of sequential calls made
			this.sequentialCallCount.incrementAndGet();

			// Output heap sizes after garbage collection
			if ((callCount.intValue() % 1000000) == 0) {
				SequentialTaskStressTest.this.printMessage("Sequential Calls=" + callCount.intValue());
			}

			// Determine if enough sequential calls
			if (callCount.intValue() < this.maxSequentialCalls) {
				// Make another sequential call
				flow.doFlow(new Integer(callCount.intValue() + 1), null);
			}
		}
	}

}