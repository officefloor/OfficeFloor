/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
public class BackPressureTeamTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure back pressure notified to caller.
	 */
	public void testFlowBackPressure() throws Exception {
		this.doBackPressureTest("flow", (work) -> {
			assertSame("Should propagate the failure through callback", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION,
					work.failure);
		});
	}

	/**
	 * Ensure back pressure propagated.
	 */
	public void testNextBackPressure() throws Exception {
		this.doBackPressureTest("next", (work) -> {
			assertNull("No callback, so no capture", work.failure);
		});
	}

	/**
	 * Ensure back pressure notified to caller even if via {@link Team}.
	 */
	public void testFlowBackPressureThroughTeam() throws Exception {
		this.doBackPressureTest("viaTeamFlow", (work) -> {
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
		this.doBackPressureTest("viaTeamNext", (work) -> {
			assertNull("No callback, so no capture", work.failure);
			assertNull("Again no callback with team, so no capture", work.teamFailure);
		});
	}

	/**
	 * Ensure for new functionality that attempts the {@link Team} again.
	 */
	public void testAttemptOverloadedTeamAgain() throws Exception {
		BackPressureTeamSource.resetBackPressureEscalationCount(0);
		this.doBackPressureTest("attemptAgain", (work) -> {
			assertSame("Should propagate failure in first attempt", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION,
					work.firstAttemptFailure);
			assertSame("Should propagate failure in second attempt", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION,
					work.secondAttemptFailue);
		});
		assertEquals("Should be back pressure failure for each attempt", 2,
				BackPressureTeamSource.getBackPressureEscalationCount());
	}

	/**
	 * Ensure for new functionality that attempts the {@link Team} again via new
	 * {@link ThreadState}.
	 */
	public void testAttemptOverloadedTeamAgainViaThread() throws Exception {
		BackPressureTeamSource.resetBackPressureEscalationCount(0);
		this.doBackPressureTest("attemptThreadAgain", (work) -> {
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
	 */
	private void doBackPressureTest(String functionName, Consumer<TestWork> validator) throws Exception {

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
		try {
			this.invokeFunction(functionName, null);
			fail("Should fail due to back pressure");
		} catch (RejectedExecutionException ex) {
			assertSame("Should propagate the back pressure exception", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION,
					ex);
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

				// Try again
				flow.doFlow(null, (secondEscalation) -> {
					this.secondAttemptFailue = secondEscalation;

					// Propagate failure
					throw secondEscalation;
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
				throw escalation;
			});
		}

		public void next() {
			// ensure next also propagates the back pressure
		}

		public void backPressure() throws Exception {
			fail("Should not be invoked due to back pressure of team");
		}
	}

}