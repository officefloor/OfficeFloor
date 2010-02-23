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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.NoInitialTaskException;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.ProcessContextListener;

/**
 * <p>
 * {@link Team} to re-use the invoking {@link Thread} of the
 * {@link ProcessState}. Typically this will be the {@link Thread} invoking the
 * {@link Work} via the {@link WorkManager}.
 * <p>
 * To enable the invoking {@link Thread} to be available for executing
 * {@link Task} instances of the {@link ProcessState}, the {@link Work} by the
 * {@link WorkManager} needs to be invoked with the
 * {@link #doWork(WorkManager, Object)} method.
 * <p>
 * As the typical focus of this {@link Team} is to integration with Application
 * Servers (using correct {@link Thread} for JNDI lookups), assigned {@link Job}
 * instances without a {@link ProcessState} context {@link Thread} will be
 * executed passively (i.e. by the caller - {@link Thread} of previous
 * {@link Job}).
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessContextTeam implements Team, ProcessContextListener {

	/**
	 * <p>
	 * Wrap invoking {@link Work} on the {@link WorkManager} to allow the
	 * {@link Thread} to be available to execute the {@link Task} instances of
	 * the {@link ProcessState}.
	 * <p>
	 * This method blocks until the invoked {@link ProcessState} of the invoked
	 * {@link Work} is complete.
	 * 
	 * @param workManager
	 *            {@link WorkManager} managing the {@link Work} to invoked.
	 * @param parameter
	 *            Parameter for the initial {@link Task} of the {@link Work}.
	 * @throws NoInitialTaskException
	 *             If {@link Work} of the {@link WorkManager} has no initial
	 *             {@link Task}.
	 */
	public static void doWork(WorkManager workManager, Object parameter)
			throws NoInitialTaskException {

		// Invoke the work
		ProcessFuture future = workManager.invokeWork(parameter);

		// Loop until processing complete
		while (!future.isComplete()) {

			// TODO determine how know immediately that processing completes
		}
	}

	/**
	 * {@link JobExecutor} to be used if no context {@link Thread} available.
	 */
	private final JobExecutor passiveJobExecutor = new JobExecutor();

	/**
	 * Mapping of Process Identifier to its context {@link Thread}.
	 */
	private final Map<Object, Thread> processContextThreads = new HashMap<Object, Thread>(
			50);

	/**
	 * Flag indicating whether to continue execution of {@link Job} instances.
	 */
	private volatile boolean isContinueExecution = true;

	/*
	 * ========================== Team ====================================
	 */

	@Override
	public void startWorking() {
		// Nothing to start
	}

	@Override
	public void assignJob(Job job) {

		// Execute the job
		this.passiveJobExecutor.doJob(job);

	}

	@Override
	public void stopWorking() {
		// TODO once using context threads, likely need to clean up here
	}

	/*
	 * ================== ProcessContextListener =====================
	 */

	@Override
	public void processCreated(Object processIdentifier) {

		// Obtain the current thread as the context thread
		Thread contextThread = Thread.currentThread();

		// Register context thread for process context (identifier)
		synchronized (this.processContextThreads) {
			this.processContextThreads.put(processIdentifier, contextThread);
		}
	}

	@Override
	public void processCompleted(Object processIdentifier) {
		// TODO implement ProcessContextListener.processCompleted
		throw new UnsupportedOperationException(
				"TODO implement ProcessContextListener.processCompleted");
	}

	/**
	 * Executes {@link Job} instances.
	 */
	private class JobExecutor implements JobContext {

		/**
		 * Cached time.
		 */
		private long time = -1;

		/**
		 * Executes the {@link Job}.
		 * 
		 * @param job
		 *            {@link Job} to be executed.
		 */
		public void doJob(Job job) {

			// Execute the Job until it flags complete
			boolean isComplete = false;
			while (!isComplete) {

				// Reset time
				this.time = -1;

				// Execute the job
				isComplete = job.doJob(this);
			}
		}

		/*
		 * ==================== JobContext ===========================
		 */

		@Override
		public long getTime() {

			// Ensure have the time
			if (this.time < 0) {
				this.time = System.currentTimeMillis();
			}

			// Return the time
			return this.time;
		}

		@Override
		public boolean continueExecution() {
			return ProcessContextTeam.this.isContinueExecution;
		}
	}

}