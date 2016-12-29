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
package net.officefloor.frame.integrate.process;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Tests that the {@link #notifyAll()} is invoked on the {@link ProcessFuture}
 * on completion of the {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessFutureNotifyAllTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensures the {@link #notifyAll()} is invoked on the {@link ProcessFuture}
	 * on completion of the {@link ProcessState}.
	 */
	public void testNotifyAllInvokedOnProcessCompletion() throws Exception {

		// Obtain the Office name
		String officeName = this.getOfficeName();

		// Create the work
		MockWork work = new MockWork();

		// Register the work
		final String WORK_NAME = "WORK";
		ReflectiveFunctionBuilder workBuilder = this.constructWork(work, WORK_NAME,
				"task");
		workBuilder.buildTask("task", "TEAM");

		// Register the team (threaded to allow notify of completed process)
		this.constructTeam("TEAM",
				new OnePersonTeam("TEST",
						MockTeamSource.createTeamIdentifier(), 100));

		// Construct and open the OfficeFloor
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();

		// Obtain the Work Manager
		WorkManager workManager = officeFloor.getOffice(officeName)
				.getWorkManager(WORK_NAME);

		// Invoke the work's initial task
		ProcessFuture processFuture = workManager.invokeWork(null);

		// Wait on being notified for large amount of time.
		// (Should be notified before this time)
		final long MAX_WAIT_TIME = 60000; // one minute
		long startTime = System.currentTimeMillis();
		synchronized (processFuture) {
			assertFalse("Process should not yet be complete",
					processFuture.isComplete());
			processFuture.wait(MAX_WAIT_TIME);
		}

		// Ensure that have waited less than maximum time
		long endTime = System.currentTimeMillis();
		long waitTime = endTime - startTime;
		assertTrue("Should be notified rather than wait max time",
				waitTime < MAX_WAIT_TIME);

		// Close the OfficeFloor
		officeFloor.closeOfficeFloor();
	}

	/**
	 * {@link Work} to be invoked.
	 */
	public class MockWork {

		/**
		 * {@link ManagedFunction} to be executed.
		 */
		public void task() throws Exception {
			// Wait some time to allow ProcessFuture wait to occur
			Thread.sleep(100);
		}
	}

}