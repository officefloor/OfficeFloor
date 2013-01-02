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
package net.officefloor.frame.impl.spi.team;

import org.easymock.AbstractMatcher;

import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.test.MockTeamSource;
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
			} else if (this.failure instanceof Error) {
				throw (Error) this.failure;
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
		Team team = new ProcessContextTeam(
				MockTeamSource.createTeamIdentifier());

		// Create the mock task (completes immediately)
		MockTaskContainer task = new MockTaskContainer();
		task.stopProcessing = true;

		// Run team and execute a task
		team.startWorking();
		team.assignJob(task, MockTeamSource.createTeamIdentifier());
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

		// Create the team
		final ProcessContextTeam team = new ProcessContextTeam(
				MockTeamSource.createTeamIdentifier());

		// Mocks
		final WorkManager workManager = this
				.createSynchronizedMock(WorkManager.class);

		// Helper Objects
		final Object processIdentifier = "ProcessIdentifier";
		final String parameter = "Parameter";

		// Objects to invoke work and block context thread for use
		final boolean[] isProcessingStarted = new boolean[1];
		isProcessingStarted[0] = false; // processing not yet started
		final boolean[] isProcessComplete = new boolean[1];
		isProcessComplete[0] = false; // not yet complete
		final ProcessFuture processFuture = new ProcessFuture() {
			@Override
			public boolean isComplete() {

				// Flag processing started
				synchronized (isProcessingStarted) {
					isProcessingStarted[0] = true;
					isProcessingStarted.notify();
				}

				// Return whether completed processing
				synchronized (isProcessComplete) {
					return isProcessComplete[0];
				}
			}
		};

		// Record invoking work
		this.recordReturn(workManager, workManager.invokeWork(parameter),
				processFuture, new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						assertEquals("Incorrect parameter", parameter,
								actual[0]);

						// Associate Thread to process identifier.
						// This would occur during the creation of the Process
						// for the invoked work.
						team.processCreated(processIdentifier);

						return true;
					}
				});

		// Run test
		this.replayMockObjects();

		// Start the team
		team.startWorking();

		// Do work with the context thread
		final Thread contextThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// Do the work (blocking call)
					ProcessContextTeam.doWork(workManager, parameter);

				} catch (Exception ex) {
					// Flag testing failed
					ProcessContextTeamTest.this.failure = ex;
				}
			}
		}, "CONTEXT_THREAD");
		contextThread.start();

		// Wait until processing starts
		synchronized (isProcessingStarted) {
			if (!isProcessingStarted[0]) {
				isProcessingStarted.wait(10000);
			}
			assertTrue("Processing should be started", isProcessingStarted[0]);
		}

		// Have Team execute Job with context Thread
		final boolean[] isJobExecuted = new boolean[1];
		isJobExecuted[0] = false;
		team.assignJob(new Job() {
			@Override
			public Object getProcessIdentifier() {
				return processIdentifier;
			}

			@Override
			public boolean doJob(JobContext executionContext) {
				try {
					// Obtain the Thread executing this job
					Thread executingThread = Thread.currentThread();

					// Ensure same as context Thread
					assertEquals("Incorrect executing Thread", contextThread,
							executingThread);

				} catch (Throwable ex) {
					ProcessContextTeamTest.this.failure = ex;
				}

				// Notify Job executed
				synchronized (isJobExecuted) {
					isJobExecuted[0] = true;
					isJobExecuted.notify();
				}

				// Job complete
				return true;
			}

			private Job nextJob;

			@Override
			public Job getNextJob() {
				return this.nextJob;
			}

			@Override
			public void setNextJob(Job job) {
				this.nextJob = job;
			}
		}, MockTeamSource.createTeamIdentifier());

		// Ensure Job to be executed
		synchronized (isJobExecuted) {
			isJobExecuted.wait(10000);
			assertTrue("Job should be executed", isJobExecuted[0]);
		}

		// Flag process complete
		team.processCompleted(processIdentifier);

		// Stop team
		team.stopWorking();

		// Verify functionality
		this.verifyMockObjects();
	}

}