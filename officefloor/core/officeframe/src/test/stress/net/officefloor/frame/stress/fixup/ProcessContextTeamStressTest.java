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
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.ThreadLocalAwareTeamSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Stress tests the {@link ThreadLocalAwareTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessContextTeamStressTest extends AbstractOfficeConstructTestCase {

	/**
	 * Potential failure of test.
	 */
	private volatile Throwable failure;

	/**
	 * Stress tests the {@link ThreadLocalAwareTeam}.
	 */
	@StressTest
	public void testStressProcessContextTeam() throws Throwable {

		final int CONTEXT_WORK_COUNT = 50;
		final int MAX_INVOKE_COUNT = 10000;

		final String officeName = this.getOfficeName();

		// Provide the teams
		this.constructTeam("CONTEXT_TEAM", ThreadLocalAwareTeamSource.class);
		this.constructTeam("STATIC_TEAM", new OnePersonTeam("STATIC_TEAM", 100));

		// Create the context parameters
		ContextParameter[] contextParameters = new ContextParameter[CONTEXT_WORK_COUNT];
		for (int i = 0; i < contextParameters.length; i++) {
			// Create the context parameter
			contextParameters[i] = new ContextParameter(MAX_INVOKE_COUNT);
		}

		// Construct the static work
		StaticWork staticWork = new StaticWork();
		ReflectiveFunctionBuilder staticTask = this.constructFunction(staticWork, "staticTask");
		staticTask.getBuilder().setResponsibleTeam("STATIC_TEAM");
		staticTask.buildParameter();
		staticTask.buildFlow("contextTask", ContextParameter.class, false);

		// Construct the context work
		ContextWork contextWork = new ContextWork();
		ReflectiveFunctionBuilder contextTask = this.constructFunction(contextWork, "contextTask");
		contextTask.getBuilder().setResponsibleTeam("CONTEXT_TEAM");
		contextTask.buildParameter();
		contextTask.buildFlow("staticTask", ContextParameter.class, false);

		// Construct and open the OfficeFloor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Obtain the Function Manager
		FunctionManager functionManager = officeFloor.getOffice(officeName).getFunctionManager("staticTask");

		// Run the context processing
		for (ContextParameter parameter : contextParameters) {
			parameter.setFunctionManager(functionManager);
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
	 * Functionality run on a different {@link Thread}/{@link Team} to have
	 * execution switching between {@link Thread} instances.
	 */
	public class StaticWork {

		/**
		 * Static {@link ManagedFunction} that all execution runs through.
		 * 
		 * @param parameter
		 *            {@link ContextParameter}.
		 * @param flow
		 *            {@link ReflectiveFlow} to be invoked.
		 */
		public void staticTask(ContextParameter parameter, ReflectiveFlow flow) {
			try {

				// Invoke the flow
				flow.doFlow(parameter, null);

			} catch (Throwable ex) {
				ProcessContextTeamStressTest.this.failure = ex;
			}
		}
	}

	/**
	 * Functionality requiring {@link ManagedFunction} to be executed with a
	 * context {@link Thread}.
	 */
	public class ContextWork extends Thread {

		/**
		 * {@link ManagedFunction} to be executed with the context
		 * {@link Thread}.
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
					flow.doFlow(parameter, null);
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
		 * Number of times to have the context {@link ManagedFunction} invoked.
		 */
		private final int maxInvokeCount;

		/**
		 * Number of times that the {@link ManagedFunction} has been invoked.
		 * This is not <code>volatile</code> as should always be the same
		 * {@link Thread} executing the {@link ManagedFunction}.
		 */
		private int invokeCount = 0;

		/**
		 * {@link FunctionManager} to invoke the {@link ManagedFunction}.
		 */
		private FunctionManager functionManager;

		/**
		 * Flag indicating if the processing is complete.
		 */
		public volatile boolean isComplete = false;

		/**
		 * Initiate.
		 * 
		 * @param maxInvokeCount
		 *            Number of times to have the context
		 *            {@link ManagedFunction} invoked.
		 */
		public ContextParameter(int maxInvokeCount) {
			this.maxInvokeCount = maxInvokeCount;
		}

		/**
		 * Specifies the {@link FunctionManager}.
		 * 
		 * @param workManager
		 *            {@link FunctionManager}.
		 */
		public synchronized void setFunctionManager(FunctionManager functionManager) {
			this.functionManager = functionManager;
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

				// Obtain the FunctionManager
				FunctionManager manager;
				synchronized (this) {
					manager = this.functionManager;
				}

				// Invoke the function
				//ThreadLocalAwareTeam.doFunction(manager, this);
				fail("No longer require wrapping doFunction, as handled internally if require thread local awareness");

				// Ensure correct number of invocations.
				// No need to synchronise as should be on the same Thread.
				assertEquals("Incorrect number of context task invocations", this.maxInvokeCount, this.invokeCount);

			} catch (Throwable ex) {
				ProcessContextTeamStressTest.this.failure = ex;
			} finally {
				// Flag that processing complete
				this.isComplete = true;
			}
		}
	}

}