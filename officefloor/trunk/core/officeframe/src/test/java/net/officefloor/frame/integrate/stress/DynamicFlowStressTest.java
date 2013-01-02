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
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Ensure dynamically invoking {@link Task} instances is stress tested.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamicFlowStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure no issues arise in dynamic flow stressing for a
	 * {@link OnePersonTeam}.
	 */
	@StressTest
	public void test_StressDynamicFlow_OnePersonTeam() throws Exception {
		this.doTest(new OnePersonTeam("TEST", MockTeamSource
				.createTeamIdentifier(), 100));
	}

	/**
	 * Ensure no issues arise in dynamic flow stressing for a
	 * {@link LeaderFollowerTeam}.
	 */
	@StressTest
	public void test_StressDynamicFlow_LeaderFollowerTeam() throws Exception {
		this.doTest(new LeaderFollowerTeam("TEST", MockTeamSource
				.createTeamIdentifier(), 3, 100));
	}

	/**
	 * Ensure no issues arise in dynamic flow stressing for a
	 * {@link ExecutorFixedTeamSource}.
	 */
	@StressTest
	public void test_StressDynamicFlow_ExecutorFixedTeam() throws Exception {
		this.doTest(ExecutorFixedTeamSource.createTeam("TEST",
				MockTeamSource.createTeamIdentifier(), 3));
	}

	/**
	 * Does the dynamic flow stress test.
	 * 
	 * @param team
	 *            {@link Team} to execute {@link Task} instances.
	 */
	private void doTest(Team team) throws Exception {

		final int MAX_COUNT = 5000000;
		int MAX_RUN_TIME = 100;
		this.setVerbose(true);

		// Construct team
		this.constructTeam("TEAM", team);

		// Construct tasks. Asynchronous instigated to:
		// 1) reset thread state for next dynamic invocation
		// 2) not have too many linked JobNodes causing OOM
		DynamicInvokeFlowWork work = new DynamicInvokeFlowWork(MAX_COUNT);
		ReflectiveWorkBuilder builder = this.constructWork(work, "WORK",
				"initialTask");
		ReflectiveTaskBuilder initialTask = builder.buildTask("initialTask",
				"TEAM");
		initialTask.buildTaskContext();
		ReflectiveTaskBuilder dynamicTask = builder.buildTask("dynamicTask",
				"TEAM");
		dynamicTask.buildFlow("initialTask",
				FlowInstigationStrategyEnum.ASYNCHRONOUS, null);
		dynamicTask.buildParameter();

		// Run stress test
		this.invokeWork("WORK", null, MAX_RUN_TIME);

		// Ensure correct number of invocations
		assertEquals("Incorrect number of invocations", MAX_COUNT,
				work.invocationCount);
	}

	/**
	 * Mock {@link Work} for testing.
	 */
	public class DynamicInvokeFlowWork {

		/**
		 * Maximum number of invocations.
		 */
		private final int maxInvocations;

		/**
		 * Number of times dynamic flow invoked.
		 */
		public volatile int invocationCount = 0;

		/**
		 * Initiate.
		 * 
		 * @param maxInvocations
		 *            Maximum number of invocations.
		 */
		public DynamicInvokeFlowWork(int maxInvocations) {
			this.maxInvocations = maxInvocations;
		}

		/**
		 * Invokes the {@link JobSequence} dynamically.
		 * 
		 * @param context
		 *            {@link TaskContext}.
		 */
		public void initialTask(TaskContext<?, ?, ?> context) throws Exception {
			// Invoke the dynamic flow
			context.doFlow("WORK", "dynamicTask", new Integer(
					this.invocationCount));
		}

		/**
		 * {@link Task} to invoke dynamically.
		 * 
		 * @param flow
		 *            {@link ReflectiveFlow}.
		 * @param currentInvocationCount
		 *            Invocation count passed as parameter from
		 *            <code>initialTask</code>.
		 */
		public void dynamicTask(ReflectiveFlow flow,
				Integer currentInvocationCount) {

			// Ensure correct current count
			assertEquals("Incorrect current invocation count",
					this.invocationCount, currentInvocationCount.intValue());

			// Increment the number of invocations
			this.invocationCount++;

			// Provide progress
			if ((this.invocationCount % (this.maxInvocations / 10)) == 0) {
				DynamicFlowStressTest.this.printMessage("Processed "
						+ this.invocationCount + " dynamic flow invocations");
			}

			// Invoke another invocation if not yet max invocations
			if (this.invocationCount < this.maxInvocations) {
				flow.doFlow(null);
			}
		}
	}

}