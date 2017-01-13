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
package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensures the appropriate {@link Team} is used to execute each
 * {@link ManagedFunction}.
 *
 * @author Daniel Sagenschneider
 */
public class AppropriateFlowResponsibleTeamTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can invoke {@link Flow} with any {@link Team} without
	 * {@link FlowCallback}.
	 */
	public void testAnyTeamFlowWithoutCallback() throws Exception {
		this.doAppropriateResponsibleTeamTest(false, false, false);
	}

	/**
	 * Ensure can invoke {@link Flow} without {@link FlowCallback}.
	 */
	public void testFlowWithoutCallback() throws Exception {
		this.doAppropriateResponsibleTeamTest(true, false, false);
	}

	/**
	 * Ensure can invoke {@link Flow} with any {@link Team} with
	 * {@link FlowCallback}.
	 */
	public void testAnyTeamFlowWithCallback() throws Exception {
		this.doAppropriateResponsibleTeamTest(false, true, false);
	}

	/**
	 * Ensure can invoke {@link Flow} with {@link FlowCallback}.
	 */
	public void testFlowWithCallback() throws Exception {
		this.doAppropriateResponsibleTeamTest(true, true, false);
	}

	/**
	 * Ensure can invoke {@link ThreadState} with any {@link Team} without
	 * {@link FlowCallback}.
	 */
	public void testAnyTeamThreadStateWithoutCallback() throws Exception {
		this.doAppropriateResponsibleTeamTest(false, false, true);
	}

	/**
	 * Ensure can invoke {@link ThreadState} without {@link FlowCallback}.
	 */
	public void testThreadStateWithoutCallback() throws Exception {
		this.doAppropriateResponsibleTeamTest(true, false, true);
	}

	/**
	 * Ensure can invoke {@link ThreadState} with any {@link Team} with a
	 * {@link FlowCallback}.
	 */
	public void testAnyTeamThreadStateWithCallback() throws Exception {
		this.doAppropriateResponsibleTeamTest(false, true, true);
	}

	/**
	 * Ensure can invoke {@link ThreadState} with a {@link FlowCallback}.
	 */
	public void testThreadStateWithCallback() throws Exception {
		this.doAppropriateResponsibleTeamTest(true, true, true);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param isFlowHaveTeam
	 *            Whether the {@link Flow} has a responsible {@link Team}
	 *            assigned.
	 * @param isProvideCallback
	 *            Whether to provide a {@link FlowCallback}.
	 * @param isSpawnThreadState
	 *            Whether to spawn a {@link ThreadState}.
	 */
	public void doAppropriateResponsibleTeamTest(boolean isFlowHaveTeam, boolean isProvideCallback,
			boolean isSpawnThreadState) throws Exception {

		// Construct the teams
		OnePersonTeam taskTeam = new OnePersonTeam("TASK", 10);
		this.constructTeam("TASK_TEAM", taskTeam);
		OnePersonTeam flowTeam = null;
		if (isFlowHaveTeam) {
			flowTeam = new OnePersonTeam("FLOW", 10);
			this.constructTeam("FLOW_TEAM", flowTeam);
		}

		// Create the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.getBuilder().setTeam("TASK_TEAM");
		task.buildParameter();
		task.buildFlow("flow", null, false);
		ReflectiveFunctionBuilder flow = this.constructFunction(work, "flow");
		if (isFlowHaveTeam) {
			flow.getBuilder().setTeam("FLOW_TEAM");
		}

		// Invoke the function
		this.invokeFunction("task", isProvideCallback);

		// Ensure appropriate threads used
		assertTrue("Task should be invoked", work.isTaskInvoked);
		assertEquals("Incorrect task team", work.taskThreadName, taskTeam.getTeamName());
		assertEquals("Incorrect flow team", work.flowThreadName,
				(isFlowHaveTeam ? flowTeam.getTeamName() : taskTeam.getTeamName()));
		if (isProvideCallback) {
			assertEquals("Incorrect callback team", work.callbackThreadName, taskTeam.getTeamName());
		} else {
			assertNull("Should not have callback team, as not invoked", work.callbackThreadName);
		}
	}

	/**
	 * Functionality.
	 */
	public class TestWork {

		public volatile boolean isTaskInvoked = false;

		public volatile String taskThreadName = null;

		public volatile String flowThreadName = null;

		public volatile String callbackThreadName = null;

		public void task(Boolean isProvideCallback, ReflectiveFlow flow) {
			this.isTaskInvoked = true;
			this.taskThreadName = Thread.currentThread().getName();
			flow.doFlow(null, isProvideCallback
					? (escalation) -> this.callbackThreadName = Thread.currentThread().getName() : null);
		}

		public void flow() {
			this.flowThreadName = Thread.currentThread().getName();
		}
	}

}