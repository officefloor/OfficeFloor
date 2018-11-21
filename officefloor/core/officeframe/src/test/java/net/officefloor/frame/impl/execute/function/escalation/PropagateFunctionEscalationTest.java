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
package net.officefloor.frame.impl.execute.function.escalation;

import java.util.function.BiConsumer;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.ExecutorCachedTeamSource;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure {@link EscalationHandler} instances can propagate failure to handle
 * {@link Escalation}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropagateFunctionEscalationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Failure to propagate.
	 */
	private final RuntimeException propagateFailure = new RuntimeException("TEST PROPAGATE");

	/**
	 * Ensure main {@link ThreadState} {@link Escalation} is propagated out of
	 * {@link OfficeFloor}.
	 */
	public void testFailure() throws Exception {
		this.doPropagateTest("failure", (work, escalation) -> {
			assertSame("Should propagate main thread escalation out of OfficeFloor", this.propagateFailure, escalation);
		});
	}

	/**
	 * Ensure {@link FlowCallback} can propagate the {@link Escalation} (or fail
	 * itself).
	 */
	public void testFlowPropagation() throws Exception {
		this.doPropagateTest("flow", (work, escalation) -> {
			assertSame("Should be handled by flow callback", this.propagateFailure, work.flowFailure);
			assertSame("Should propagate out as on main thread", this.propagateFailure, escalation);
		});
	}

	/**
	 * Ensure that {@link Team} should make little difference in
	 * {@link FlowCallback} {@link Escalation} propagation.
	 */
	public void testTeamPropagation() throws Exception {
		this.doPropagateTest("viaTeam", (work, escalation) -> {
			assertSame("Should be handled by flow callback", this.propagateFailure, work.flowFailure);
			assertSame("Team attempted flow callback", this.propagateFailure, work.teamFailure);
			assertSame("Should propagate out as on main thread", this.propagateFailure, escalation);
		});
	}

	/**
	 * Ensure that {@link ThreadState} can handle propagation of {@link Escalation}.
	 */
	public void testThreadHandling() throws Exception {
		this.doPropagateTest("viaThread", (work, escalation) -> {
			assertSame("Should be handled by flow callback", this.propagateFailure, work.flowFailure);
			assertSame("Team attempted flow callback", this.propagateFailure, work.teamFailure);
			assertSame("Thread attempted flow callback", this.propagateFailure, work.threadFailure);
			assertSame("Should propagate through main thread out of OfficeFloor", this.propagateFailure, escalation);
		});
	}

	/**
	 * Undertakes the propagate test.
	 * 
	 * @param functionName Name of {@link ManagedFunction}.
	 * @param validator    Validates the {@link TestWork} and the possible
	 *                     {@link Escalation} propagation from {@link OfficeFloor}.
	 */
	private void doPropagateTest(String functionName, BiConsumer<TestWork, Throwable> validator) throws Exception {

		// Construct the functions
		TestWork work = new TestWork();

		// Thread -> team -> flow -> failure
		ReflectiveFunctionBuilder viaThread = this.constructFunction(work, "viaThread");
		viaThread.buildFlow("viaTeam", null, true);

		// Team -> flow -> failure
		this.constructTeam("VIA_TEAM", ExecutorCachedTeamSource.class);
		ReflectiveFunctionBuilder viaTeamFlow = this.constructFunction(work, "viaTeam");
		viaTeamFlow.getBuilder().setResponsibleTeam("VIA_TEAM");
		viaTeamFlow.buildFlow("flow", null, false);

		// Flow -> failure
		ReflectiveFunctionBuilder flow = this.constructFunction(work, "flow");
		flow.buildFlow("failure", null, false);

		// Failure
		this.constructFunction(work, "failure");

		// Invoke the function
		Throwable officeFloorEscalation = null;
		try {
			this.invokeFunction(functionName, null);
		} catch (Throwable ex) {
			officeFloorEscalation = ex;
		}

		// Undertake validation
		validator.accept(work, officeFloorEscalation);
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		private volatile Throwable flowFailure = null;

		private volatile Throwable teamFailure = null;

		private volatile Throwable threadFailure = null;

		public void viaThread(ReflectiveFlow flow) {
			flow.doFlow(null, (escalation) -> {
				this.threadFailure = escalation;
				throw escalation;
			});
		}

		public void viaTeam(ReflectiveFlow flow) {
			flow.doFlow(null, (escalation) -> {
				this.teamFailure = escalation;
				throw escalation;
			});
		}

		public void flow(ReflectiveFlow flow) {
			flow.doFlow(null, (escalation) -> {
				this.flowFailure = escalation;
				throw escalation;
			});
		}

		public void failure() throws Throwable {
			throw PropagateFunctionEscalationTest.this.propagateFailure;
		}
	}

}