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
package net.officefloor.frame.integrate.work;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.NoInitialTaskException;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests {@link Work} being invoked from the {@link WorkManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures the initial {@link ManagedFunction} is executed for the {@link Work}.
	 */
	public void testWithInitialTask() throws Exception {

		// Create the work
		TestWork work = new TestWork();

		// Register the work
		final String WORK_NAME = "WORK";
		ReflectiveFunctionBuilder workBuilder = this.constructWork(work, WORK_NAME,
				"taskOne");
		workBuilder.buildTask("taskOne", "TEAM");

		// Register the team
		this.constructTeam("TEAM", new PassiveTeam());

		// Invoke the work's initial task
		this.invokeWork(WORK_NAME, null);

		// Ensure task invoked
		assertTrue("Initial task should be invoked", work.isTaskOneInvoked);
	}

	/**
	 * Ensures may build with no initial {@link ManagedFunction} for {@link Work} but may
	 * not invoke the {@link Work}.
	 */
	public void testNoInitialTaskForWork() throws Exception {

		// Create the work
		TestWork work = new TestWork();

		// Register the work
		final String WORK_NAME = "WORK";
		ReflectiveFunctionBuilder workBuilder = this.constructWork(work, WORK_NAME,
				null);
		workBuilder.buildTask("taskOne", "TEAM");

		// Register the team
		this.constructTeam("TEAM", new PassiveTeam());

		try {
			// Invoke the work without initial task
			this.invokeWork(WORK_NAME, null);

			// Should not be successful as no initial task
			fail("Should not invoke work as no initial task");

		} catch (NoInitialTaskException ex) {
			// Correctly indicating no initial task
		}
	}

	/**
	 * Mock work class for testing.
	 */
	public static class TestWork {

		/**
		 * Flag indicating if {@link #taskOne()} was invoked.
		 */
		public volatile boolean isTaskOneInvoked = false;

		/**
		 * Task.
		 */
		public void taskOne() {
			isTaskOneInvoked = true;
		}
	}
}
