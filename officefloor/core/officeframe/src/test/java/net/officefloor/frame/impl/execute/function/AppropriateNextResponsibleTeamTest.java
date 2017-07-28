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

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensures the appropriate {@link Team} is used to execute each
 * {@link ManagedFunction}.
 *
 * @author Daniel Sagenschneider
 */
public class AppropriateNextResponsibleTeamTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure can invoke next {@link ManagedFunction} with responsible
	 * {@link Team}.
	 */
	public void testNextFunctionWithAppropriateTeam() throws Exception {
		this.doNextFunctionWithAppropriateTeamTest(true);
	}

	/**
	 * Enure can invoke next {@link ManagedFunction} with any (same) responsible
	 * {@link Team}.
	 * 
	 * @throws Exception
	 */
	public void testNextFunctionWithSameTeam() throws Exception {
		this.doNextFunctionWithAppropriateTeamTest(false);
	}

	/**
	 * Ensure can invoke next {@link ManagedFunction} with an appropriate
	 * {@link Team}.
	 * 
	 * @param isNextHaveTeam
	 *            Whether next {@link ManagedFunction} has an assigned
	 *            responsible {@link Team}.
	 */
	public void doNextFunctionWithAppropriateTeamTest(boolean isNextHaveTeam) throws Exception {

		// Construct the teams
		OnePersonTeam taskTeam = OnePersonTeamSource.createOnePersonTeam("TASK");
		this.constructTeam("TASK_TEAM", taskTeam);
		OnePersonTeam nextTeam = null;
		if (isNextHaveTeam) {
			nextTeam = OnePersonTeamSource.createOnePersonTeam("FLOW");
			this.constructTeam("NEXT_TEAM", nextTeam);
		}

		// Create the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder task = this.constructFunction(work, "task");
		task.getBuilder().setResponsibleTeam("TASK_TEAM");
		task.getBuilder().setNextFunction("next", null);
		ReflectiveFunctionBuilder flow = this.constructFunction(work, "next");
		if (isNextHaveTeam) {
			flow.getBuilder().setResponsibleTeam("NEXT_TEAM");
		}

		// Invoke the function
		this.invokeFunction("task", null);

		// Ensure appropriate threads used
		assertTrue("Task should be invoked", work.isTaskInvoked);
		assertEquals("Incorrect task team", work.taskThreadName, taskTeam.getThreadName());
		assertEquals("Incorrect next team", work.nextThreadName,
				(isNextHaveTeam ? nextTeam.getThreadName() : taskTeam.getThreadName()));
	}

	/**
	 * Functionality.
	 */
	public class TestWork {

		public volatile boolean isTaskInvoked = false;

		public volatile String taskThreadName = null;

		public volatile String nextThreadName = null;

		public void task() {
			this.isTaskInvoked = true;
			this.taskThreadName = Thread.currentThread().getName();
		}

		public void next() {
			this.nextThreadName = Thread.currentThread().getName();
		}
	}

}