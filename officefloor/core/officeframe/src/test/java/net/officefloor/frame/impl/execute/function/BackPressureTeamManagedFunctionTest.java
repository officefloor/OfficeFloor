/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.frame.impl.execute.function;

import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.BackPressureTeamSource;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensures appropriately handles the back pressure.
 * 
 * @author Daniel Sagenschneider
 */
public class BackPressureTeamManagedFunctionTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure back pressure notified to caller.
	 */
	public void testFlowBackPressure() throws Exception {
		this.doBackPressureTest("flow", true, (work) -> {
			assertFalse("Should not invoke back pressure method", work.isBackPressureInvoked);
			assertSame("Should propagate the failure through callback", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION,
					work.failure);
		});
	}

	/**
	 * Ensure back pressure propagated.
	 */
	public void testNextBackPressure() throws Exception {
		this.doBackPressureTest("next", true, (work) -> {
			assertFalse("Should not invoke back pressure method", work.isBackPressureInvoked);
			assertNull("No callback, so no capture", work.failure);
		});
	}

	/**
	 * Ensure back pressure notified to caller even if via {@link Team}.
	 */
	public void testFlowBackPressureThroughTeam() throws Exception {
		this.doBackPressureTest("viaTeamFlow", true, (work) -> {
			assertFalse("Should not invoke back pressure method", work.isBackPressureInvoked);
			assertSame("Should propagate the failure through callback", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION,
					work.failure);
			assertSame("Should propagate the failure through team", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION,
					work.teamFailure);
		});
	}

	/**
	 * Ensure back pressure propagated even if via {@link Team}.
	 */
	public void testNextBackPressureThroughTeam() throws Exception {
		this.doBackPressureTest("viaTeamNext", true, (work) -> {
			assertFalse("Should not invoke back pressure method", work.isBackPressureInvoked);
			assertNull("No callback, so no capture", work.failure);
			assertNull("Again no callback with team, so no capture", work.teamFailure);
		});
	}

	/**
	 * Ensure for new functionality that attempts the {@link Team} again.
	 */
	public void testAttemptOverloadedTeamAgain() throws Exception {
		BackPressureTeamSource.resetBackPressureEscalationCount(0);
		this.doBackPressureTest("attemptAgain", false, (work) -> {
			assertTrue(
					"Calling parallel within function callback, allows call but with current team to slow current team",
					work.isBackPressureInvoked);
			assertSame("Calls to overloaded team should be by current team (back pressure up pipeline)",
					Thread.currentThread(), work.backPressureThread);
			assertSame("Should propagate failure in first attempt", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION,
					work.firstAttemptFailure);
			assertNull("Should allow clean up calls to functions", work.secondAttemptFailue);
		});
		assertEquals("Should be back pressure failure for each attempt", 1,
				BackPressureTeamSource.getBackPressureEscalationCount());
	}

	/**
	 * Ensure for new functionality that attempts the {@link Team} again via new
	 * {@link ThreadState}.
	 */
	public void testAttemptOverloadedTeamAgainViaThread() throws Exception {
		BackPressureTeamSource.resetBackPressureEscalationCount(0);
		this.doBackPressureTest("attemptThreadAgain", true, (work) -> {
			assertFalse("Should not invoke back pressure method (as new thread state)", work.isBackPressureInvoked);
			assertSame("Should propagate failure in first attempt", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION,
					work.firstAttemptFailure);
			assertSame("Should propagate failure in second attempt", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION,
					work.secondAttemptFailue);
		});
		assertEquals("Should be back pressure failure for each attempt", 2,
				BackPressureTeamSource.getBackPressureEscalationCount());
	}

	/**
	 * Undertakes the back pressure test.
	 * 
	 * @param functionName Name of {@link ManagedFunction}.
	 * @param validator    Validates the {@link TestWork}.
	 * @param isEscalate   Indicates if escalate the back pressure from invocation.
	 */
	private void doBackPressureTest(String functionName, boolean isEscalate, Consumer<TestWork> validator)
			throws Exception {

		// Construct the functions
		TestWork work = new TestWork();

		// Flow
		ReflectiveFunctionBuilder flow = this.constructFunction(work, "flow");
		flow.buildFlow("backPressure", null, false);

		// Next
		this.constructFunction(work, "next").setNextFunction("backPressure");

		// Construct via team
		this.constructTeam("VIA_TEAM", ExecutorCachedTeamSource.class);

		// Team -> Flow -> back pressure
		ReflectiveFunctionBuilder viaTeamFlow = this.constructFunction(work, "viaTeamFlow");
		viaTeamFlow.getBuilder().setResponsibleTeam("VIA_TEAM");
		viaTeamFlow.buildFlow("flow", null, false);

		// Team -> Next -> back pressure
		ReflectiveFunctionBuilder viaTeamNext = this.constructFunction(work, "viaTeamNext");
		viaTeamNext.getBuilder().setResponsibleTeam("VIA_TEAM");
		viaTeamNext.setNextFunction("next");

		// Attempt again
		ReflectiveFunctionBuilder attemptAgain = this.constructFunction(work, "attemptAgain");
		attemptAgain.buildFlow("flow", null, false);

		// Attempt again
		ReflectiveFunctionBuilder attemptThreadAgain = this.constructFunction(work, "attemptThreadAgain");
		attemptThreadAgain.buildFlow("flow", null, true);

		// Function causing back pressure by team
		this.constructTeam("BACK_PRESSURE", BackPressureTeamSource.class);
		this.constructFunction(work, "backPressure").getBuilder().setResponsibleTeam("BACK_PRESSURE");

		// Invoke the function
		RejectedExecutionException escalation = null;
		try {
			this.invokeFunction(functionName, null);
		} catch (RejectedExecutionException ex) {
			escalation = ex;
		}

		// Ensure appropriate escalation
		if (isEscalate) {
			assertSame("Should propagate the back pressure exception", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION,
					escalation);
		} else {
			assertNull("Should not propagate back pressure exception", escalation);
		}

		// Undertake validation
		validator.accept(work);
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		private volatile Throwable failure = null;

		private volatile Throwable teamFailure = null;

		private volatile Throwable firstAttemptFailure = null;

		private volatile Throwable secondAttemptFailue = null;

		private volatile boolean isBackPressureInvoked = false;

		private volatile Thread backPressureThread = null;

		public void attemptThreadAgain(ReflectiveFlow flow) {
			flow.doFlow(null, (escalation) -> {
				this.firstAttemptFailure = escalation;

				// Try again
				flow.doFlow(null, (secondEscalation) -> {
					this.secondAttemptFailue = secondEscalation;

					// Propagate failure
					throw secondEscalation;
				});
			});
		}

		public void attemptAgain(ReflectiveFlow flow) {
			flow.doFlow(null, (escalation) -> {
				this.firstAttemptFailure = escalation;

				// Try again (allowed to run to clean up)
				flow.doFlow(null, (secondEscalation) -> {
					this.secondAttemptFailue = secondEscalation;

					// Within cleanup scope, so should be able to call function
					assertTrue("Should invoke back pressed function (however not via responsible team)",
							this.isBackPressureInvoked);
					assertSame(
							"Should use current Team to invoke, so slows current team (causing back pressure up the pipeline)",
							Thread.currentThread(), this.backPressureThread);
				});
			});
		}

		public void viaTeamFlow(ReflectiveFlow flow) {
			flow.doFlow(null, (escalation) -> {
				this.teamFailure = escalation;
				throw escalation;
			});
		}

		public void viaTeamNext() {
			// Ensure back pressure propagated by team
		}

		public void flow(ReflectiveFlow flow) {
			flow.doFlow(null, (escalation) -> {
				this.failure = escalation;
				if (escalation != null) {
					throw escalation;
				}
			});
		}

		public void next() {
			// ensure next also propagates the back pressure
		}

		public void backPressure() throws Exception {
			this.isBackPressureInvoked = true;
			this.backPressureThread = Thread.currentThread();
		}
	}

}
