/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.ProcessContextTeam;
import net.officefloor.frame.impl.spi.team.ProcessContextTeamSource;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;

/**
 * Stress tests the {@link ProcessContextTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessContextTeamStressTest extends
		AbstractOfficeConstructTestCase {

	/**
	 * Potential failure of test.
	 */
	private volatile Throwable failure;

	/**
	 * Stress tests the {@link ProcessContextTeam}.
	 */
	@StressTest
	public void testStressProcessContextTeam() throws Throwable {

		final int CONTEXT_WORK_COUNT = 50;
		final int MAX_INVOKE_COUNT = 10000;

		final String officeName = this.getOfficeName();

		// Provide the teams
		this.constructTeam("CONTEXT_TEAM", ProcessContextTeamSource.class);
		this
				.constructTeam("STATIC_TEAM", new OnePersonTeam("STATIC_TEAM",
						100));

		// Create the context parameters
		ContextParameter[] contextParameters = new ContextParameter[CONTEXT_WORK_COUNT];
		for (int i = 0; i < contextParameters.length; i++) {
			// Create the context parameter
			contextParameters[i] = new ContextParameter(MAX_INVOKE_COUNT);
		}

		// Construct the static work
		StaticWork staticWork = new StaticWork();
		ReflectiveTaskBuilder staticTask = this.constructWork(staticWork,
				"STATIC_WORK", "staticTask").buildTask("staticTask",
				"STATIC_TEAM");
		staticTask.buildParameter();
		staticTask.buildFlow("CONTEXT_WORK", "contextTask",
				FlowInstigationStrategyEnum.SEQUENTIAL, ContextParameter.class);

		// Construct the context work
		ContextWork contextWork = new ContextWork();
		ReflectiveTaskBuilder contextTask = this.constructWork(contextWork,
				"CONTEXT_WORK", null).buildTask("contextTask", "CONTEXT_TEAM");
		contextTask.buildParameter();
		contextTask.buildFlow("STATIC_WORK", "staticTask",
				FlowInstigationStrategyEnum.SEQUENTIAL, ContextParameter.class);

		// Construct and open the OfficeFloor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Obtain the Work Manager
		WorkManager workManager = officeFloor.getOffice(officeName)
				.getWorkManager("STATIC_WORK");

		// Run the context processing
		for (ContextParameter parameter : contextParameters) {
			parameter.setWorkManager(workManager);
			parameter.start();
		}

		// Wait until all context processing complete
		boolean isComplete = false;
		while (!isComplete) {

			// Allow some time for processing
			Thread.sleep(100);

			// Determine if complete
			isComplete = true;
			for (ContextParameter parameter : contextParameters) {
				if (!parameter.isComplete) {
					isComplete = false;
				}
			}
		}

		// Processing complete, notify test of potential failure
		if (this.failure != null) {
			throw this.failure;
		}

		// Close the OfficeFloor
		officeFloor.closeOfficeFloor();
	}

	/**
	 * {@link Work} run on a different {@link Thread}/{@link Team} to have
	 * execution switching between {@link Thread} instances.
	 */
	public class StaticWork {

		/**
		 * Static {@link Task} that all execution runs through.
		 * 
		 * @param parameter
		 *            {@link ContextParameter}.
		 * @param flow
		 *            {@link ReflectiveFlow} to be invoked.
		 */
		public void staticTask(ContextParameter parameter, ReflectiveFlow flow) {
			try {

				// Invoke the flow
				flow.doFlow(parameter);

			} catch (Throwable ex) {
				ProcessContextTeamStressTest.this.failure = ex;
			}
		}
	}

	/**
	 * {@link Work} requiring {@link Task} to be executed with a context
	 * {@link Thread}.
	 */
	public class ContextWork extends Thread {

		/**
		 * {@link Task} to be executed with the context {@link Thread}.
		 * 
		 * @param parameter
		 *            {@link ContextParameter}.
		 * @param flow
		 *            {@link ReflectiveFlow} to be invoked.
		 */
		public void contextTask(ContextParameter parameter, ReflectiveFlow flow) {
			try {

				// Validate context and continue processing
				if (parameter.validateContext()) {
					flow.doFlow(parameter);
				}

			} catch (Throwable ex) {
				// Flag failure
				ProcessContextTeamStressTest.this.failure = ex;
			}
		}
	}

	/**
	 * Parameter that maintains context information.
	 */
	private class ContextParameter extends Thread {

		/**
		 * Number of times to have the context {@link Task} invoked.
		 */
		private final int maxInvokeCount;

		/**
		 * Number of times that the {@link Task} has been invoked. This is not
		 * <code>volatile</code> as should always be the same {@link Thread}
		 * executing the {@link Task}.
		 */
		private int invokeCount = 0;

		/**
		 * {@link WorkManager} to invoke the {@link Work}.
		 */
		private WorkManager workManager;

		/**
		 * Flag indicating if the processing is complete.
		 */
		public volatile boolean isComplete = false;

		/**
		 * Initiate.
		 * 
		 * @param maxInvokeCount
		 *            Number of times to have the context {@link Task} invoked.
		 */
		public ContextParameter(int maxInvokeCount) {
			this.maxInvokeCount = maxInvokeCount;
		}

		/**
		 * Specifies the {@link WorkManager}.
		 * 
		 * @param workManager
		 *            {@link WorkManager}.
		 */
		public synchronized void setWorkManager(WorkManager workManager) {
			this.workManager = workManager;
		}

		/**
		 * Validates the context.
		 * 
		 * @return <code>true</code> if to continue processing.
		 */
		public boolean validateContext() {
			try {

				// Ensure the correct Thread is executing within Context
				Thread currentThread = Thread.currentThread();
				assertEquals("Incorrect context Thread", this, currentThread);

				// Determine if continue processing
				this.invokeCount++;
				return (this.invokeCount < this.maxInvokeCount);

			} catch (Throwable ex) {
				// Flag failure
				ProcessContextTeamStressTest.this.failure = ex;
				return false; // failure so stop processing
			}
		}

		/*
		 * ======================== Thread =============================
		 */

		@Override
		public void run() {
			try {

				// Obtain the WorkManager
				WorkManager manager;
				synchronized (this) {
					manager = this.workManager;
				}

				// Invoke the work
				ProcessContextTeam.doWork(manager, this);

				// Ensure correct number of invocations.
				// No need to synchronise as should be on the same Thread.
				assertEquals("Incorrect number of context task invocations",
						this.maxInvokeCount, this.invokeCount);

			} catch (Throwable ex) {
				ProcessContextTeamStressTest.this.failure = ex;
			} finally {
				// Flag that processing complete
				this.isComplete = true;
			}
		}
	}

}