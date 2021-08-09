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

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
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
		OnePersonTeam taskTeam = OnePersonTeamSource.createOnePersonTeam("TASK");
		this.constructTeam("TASK_TEAM", taskTeam);
		OnePersonTeam flowTeam = null;
		if (isFlowHaveTeam) {
			flowTeam = OnePersonTeamSource.createOnePersonTeam("FLOW");
			this.constructTeam("FLOW_TEAM", flowTeam);
		}

		// Create the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.getBuilder().setResponsibleTeam("TASK_TEAM");
		task.buildParameter();
		task.buildFlow("flow", null, false);
		ReflectiveFunctionBuilder flow = this.constructFunction(work, "flow");
		if (isFlowHaveTeam) {
			flow.getBuilder().setResponsibleTeam("FLOW_TEAM");
		}

		// Invoke the function
		this.invokeFunction("task", isProvideCallback);

		// Ensure appropriate threads used
		assertTrue("Task should be invoked", work.isTaskInvoked);
		assertEquals("Incorrect task team", work.taskThreadName, taskTeam.getThreadName());
		assertEquals("Incorrect flow team", work.flowThreadName,
				(isFlowHaveTeam ? flowTeam.getThreadName() : taskTeam.getThreadName()));
		if (isProvideCallback) {
			assertEquals("Incorrect callback team", work.callbackThreadName, taskTeam.getThreadName());
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
