/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ProcessContextTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessContextTeamTest extends OfficeFrameTestCase {

	/**
	 * Potential failure within a created {@link Thread}.
	 */
	private volatile Throwable failure = null;

	@Override
	protected void tearDown() throws Exception {
		if (this.failure != null) {
			// Propagate threaded failure of test
			if (this.failure instanceof Exception) {
				throw (Exception) this.failure;
			} else {
				throw new Exception(this.failure);
			}
		}
	}

	/**
	 * Ensures that passively executes the {@link Job} should no {@link Thread}
	 * be available for the {@link ProcessState} context.
	 */
	public void testPassiveExecuteJob() {

		// Create the team
		Team team = new ProcessContextTeam();

		// Create the mock task (completes immediately)
		MockTaskContainer task = new MockTaskContainer();
		task.stopProcessing = true;

		// Run team and execute a task
		team.startWorking();
		team.assignJob(task);
		team.stopWorking();

		// Ensure the task executed
		assertEquals("Task should be executed once", 1,
				task.doTaskInvocationCount);
	}

	/**
	 * Ensure that executes the {@link Job} with the context {@link Thread} for
	 * the {@link ProcessState}.
	 */
	public void testExecuteJobWithContextThread() throws Exception {

		// Mocks
		final WorkManager workManager = this
				.createSynchronizedMock(WorkManager.class);

		// Objects to invoke work and block context thread for use
		final String parameter = "Parameter";
		final boolean[] isProcessingStarted = new boolean[1];
		isProcessingStarted[0] = false; // processing not yet started
		final boolean[] isProcessComplete = new boolean[1];
		isProcessComplete[0] = false; // not yet complete
		final FlowFuture flowFuture = new FlowFuture() {
			@Override
			public boolean isComplete() {

				// Flag processing started
				synchronized (isProcessingStarted) {
					isProcessingStarted[0] = true;
					isProcessingStarted.notify();
				}

				// Return whether complete processing
				synchronized (isProcessComplete) {
					return isProcessComplete[0];
				}
			}
		};

		// Record invoking work
		this.recordReturn(workManager, workManager.invokeWork(parameter),
				flowFuture);

		// Create the process identifier
		final Object processIdentifier = "ProcessIdentifier";

		// Create the team
		final ProcessContextTeam team = new ProcessContextTeam();

		// Do work with the context thread
		Thread contextThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {

					// Associate Thread to process identifier.
					// This would actually occur during the doWork call, however
					// as it is blocking need to do beforehand.
					team.processCreated(processIdentifier);

					// Do the work (blocking call)
					ProcessContextTeam.doWork(workManager, parameter);

				} catch (Exception ex) {
					// Flag testing failed
					ProcessContextTeamTest.this.failure = ex;
				}
			}
		});
		contextThread.start();

		// Wait until processing starts
		synchronized (isProcessingStarted) {
			if (!isProcessingStarted[0]) {
				isProcessingStarted.wait(1000);
			}
			assertTrue("Processing should be started", isProcessingStarted[0]);
		}

		// TODO complete test as verify correct context Thread executing Task
		fail("TODO complete test as verify correct context Thread executing Task");
	}
}