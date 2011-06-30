/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.NoInitialTaskException;
import net.officefloor.frame.api.manage.ProcessFuture;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
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
	 * Initial capacity of contexts.
	 */
	private static final int CONTEXT_THREAD_INITIAL_CAPACITY = 50;

	/**
	 * Mapping of {@link Thread} to {@link JobQueueExecutor}.
	 */
	private static final Map<Thread, JobQueueExecutor> threadToExecutor = new HashMap<Thread, JobQueueExecutor>(
			CONTEXT_THREAD_INITIAL_CAPACITY);

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
	 * @throws InvalidParameterTypeException
	 *             Should the parameter type be invalid for the {@link Task}.
	 * @throws InterruptedException
	 *             Should this blocking call be interrupted.
	 */
	public static void doWork(WorkManager workManager, Object parameter)
			throws NoInitialTaskException, InvalidParameterTypeException,
			InterruptedException {

		// Obtain the current Thread
		Thread currentThread = Thread.currentThread();
		try {

			// Register the Job Queue Executor for the Thread.
			// Must be done before invoking work to ensure available.
			JobQueueExecutor executor = new JobQueueExecutor();
			synchronized (threadToExecutor) {
				threadToExecutor.put(currentThread, executor);
			}

			// Invoke the work
			ProcessFuture future = workManager.invokeWork(parameter);

			// Blocking call to execute the Jobs
			executor.executeJobs(future);

		} finally {
			// Ensure unregister current Thread
			synchronized (threadToExecutor) {
				threadToExecutor.remove(currentThread);
			}

			// All Jobs should be completed with completion of Process
		}
	}

	/**
	 * <p>
	 * Wrap invoking {@link Task} on the {@link TaskManager} to allow the
	 * {@link Thread} to be available to execute the {@link Task} instances of
	 * the {@link ProcessState}.
	 * <p>
	 * This method blocks until the invoked {@link ProcessState} of the invoked
	 * {@link Task} is complete.
	 * 
	 * @param taskManager
	 *            {@link TaskManager} managing the {@link Task} to invoked.
	 * @param parameter
	 *            Parameter for the {@link Task}.
	 * @throws InvalidParameterTypeException
	 *             Should the parameter type be invalid for the {@link Task}.
	 * @throws InterruptedException
	 *             Should this blocking call be interrupted.
	 */
	public static void doTask(TaskManager taskManager, Object parameter)
			throws InvalidParameterTypeException, InterruptedException {

		// Obtain the current Thread
		Thread currentThread = Thread.currentThread();
		try {

			// Register the Job Queue Executor for the Thread.
			// Must be done before invoking task to ensure available.
			JobQueueExecutor executor = new JobQueueExecutor();
			synchronized (threadToExecutor) {
				threadToExecutor.put(currentThread, executor);
			}

			// Invoke the task
			ProcessFuture future = taskManager.invokeTask(parameter);

			// Blocking call to execute the Jobs
			executor.executeJobs(future);

		} finally {
			// Ensure unregister current Thread
			synchronized (threadToExecutor) {
				threadToExecutor.remove(currentThread);
			}

			// All Jobs should be completed with completion of Process
		}
	}

	/**
	 * <p>
	 * Wrap invoking {@link ProcessState} on the
	 * {@link ManagedObjectExecuteContext} to allow the {@link Thread} to be
	 * available to execute the {@link Task} instances of the
	 * {@link ProcessState}.
	 * <p>
	 * This method blocks until the invoked {@link ProcessState} is complete.
	 * 
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 * @param flowKey
	 *            {@link Flow} key.
	 * @param parameter
	 *            Parameter for the initial {@link Task}.
	 * @param managedObject
	 *            {@link ManagedObject}.
	 * @param escalationHandler
	 *            {@link EscalationHandler}. May be <code>null</code>.
	 * @throws InterruptedException
	 *             Should this blocking call be interrupted.
	 */
	public static <F extends Enum<F>> void doProcess(
			ManagedObjectExecuteContext<F> executeContext, F flowKey,
			Object parameter, ManagedObject managedObject,
			EscalationHandler escalationHandler) throws InterruptedException {
		doProcess(executeContext, flowKey.ordinal(), parameter, managedObject,
				escalationHandler);
	}

	/**
	 * <p>
	 * Wrap invoking {@link ProcessState} on the
	 * {@link ManagedObjectExecuteContext} to allow the {@link Thread} to be
	 * available to execute the {@link Task} instances of the
	 * {@link ProcessState}.
	 * <p>
	 * This method blocks until the invoked {@link ProcessState} is complete.
	 * 
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 * @param flowIndex
	 *            {@link Flow} index.
	 * @param parameter
	 *            Parameter for the initial {@link Task}.
	 * @param managedObject
	 *            {@link ManagedObject}.
	 * @param escalationHandler
	 *            {@link EscalationHandler}. May be <code>null</code>.
	 * @throws InterruptedException
	 *             Should this blocking call be interrupted.
	 */
	public static void doProcess(ManagedObjectExecuteContext<?> executeContext,
			int flowIndex, Object parameter, ManagedObject managedObject,
			EscalationHandler escalationHandler) throws InterruptedException {

		// Obtain the current Thread
		Thread currentThread = Thread.currentThread();
		try {

			// Register the Job Queue Executor for the Thread.
			// Must be done before invoking task to ensure available.
			JobQueueExecutor executor = new JobQueueExecutor();
			synchronized (threadToExecutor) {
				threadToExecutor.put(currentThread, executor);
			}

			// Invoke the process
			ProcessFuture future = executeContext.invokeProcess(flowIndex,
					parameter, managedObject, 0, escalationHandler);

			// Blocking call to execute the Jobs
			executor.executeJobs(future);

		} finally {
			// Ensure unregister current Thread
			synchronized (threadToExecutor) {
				threadToExecutor.remove(currentThread);
			}

			// All Jobs should be completed with completion of Process
		}
	}

	/**
	 * {@link JobExecutor} to be used if no context {@link Thread} available.
	 */
	private final JobExecutor passiveJobExecutor = new JobExecutor(this);

	/**
	 * Mapping of Process Identifier to its context {@link Thread}.
	 */
	private final Map<Object, Thread> processContextThreads = new HashMap<Object, Thread>(
			CONTEXT_THREAD_INITIAL_CAPACITY);

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

		// Obtain the process identifier
		Object processIdentifier = job.getProcessIdentifier();

		// Obtain the context Thread for the Process
		Thread contextThread;
		synchronized (processContextThreads) {
			contextThread = processContextThreads.get(processIdentifier);
		}

		// Determine if have context Thread
		if (contextThread != null) {
			// Obtain the Job Queue Executor
			JobQueueExecutor executor;
			synchronized (threadToExecutor) {
				executor = threadToExecutor.get(contextThread);
			}

			// Determine if have Job Queue Executor
			if (executor != null) {
				// Assign Job to executor
				executor.assignJob(job);
				return; // Job assigned
			}
		}

		// No context executor, so execute passively
		this.passiveJobExecutor.doJob(job);
	}

	@Override
	public void stopWorking() {
		// Flag to stop executing
		this.isContinueExecution = false;

		// Ensure all assigned jobs are completed
		synchronized (threadToExecutor) {
			try {
				boolean isAssignedJob;
				do {

					// Determine if assigned a job
					isAssignedJob = false;
					for (JobQueueExecutor executor : threadToExecutor.values()) {
						if (!(executor.jobQueue.isEmpty())) {
							isAssignedJob = true;
						}
					}

					// Wait some time if still assigned jobs
					if (isAssignedJob) {
						threadToExecutor.wait(100);
					}

				} while (isAssignedJob);
			} catch (InterruptedException ex) {
				// Interrupted so exit
			}
		}
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

		// Register this Team with the Executor
		JobQueueExecutor executor;
		synchronized (threadToExecutor) {
			executor = threadToExecutor.get(contextThread);
		}
		if (executor != null) {
			// Context Thread available to execute Jobs
			executor.setTeamInstance(this);
		}
	}

	@Override
	public void processCompleted(Object processIdentifier) {
		// Remove the context thread
		synchronized (this.processContextThreads) {
			this.processContextThreads.remove(processIdentifier);
		}
	}

	/**
	 * Executes {@link Job} instances from the {@link JobQueue}.
	 */
	private static class JobQueueExecutor {

		/**
		 * {@link ProcessContextTeam} instance.
		 */
		private ProcessContextTeam instance;

		/**
		 * <p>
		 * {@link JobQueue}. Initial instance until {@link ProcessFuture}
		 * instance becomes available.
		 * <p>
		 * Locking on <code>this</code> until {@link ProcessFuture} available.
		 */
		private JobQueue jobQueue = new JobQueue(this);

		/**
		 * Specifies the {@link ProcessContextTeam} instance.
		 * 
		 * @param instance
		 *            {@link ProcessContextTeam} instance.
		 */
		public synchronized void setTeamInstance(ProcessContextTeam instance) {
			this.instance = instance;
			this.notify(); // start executing Jobs immediately
		}

		/**
		 * Assigns a {@link Job} to be executed.
		 * 
		 * @param job
		 *            {@link Job}.
		 * @param instance
		 *            {@link ProcessContextTeam}.
		 */
		public synchronized void assignJob(Job job) {
			this.jobQueue.enqueue(job);
		}

		/**
		 * Blocking call to execute the {@link Job} instances until completion
		 * of the {@link ProcessFuture}.
		 * 
		 * @param future
		 *            {@link ProcessFuture}.
		 */
		public void executeJobs(ProcessFuture future) {

			// Ensure coordinate with assigning Jobs
			JobExecutor executor;
			synchronized (this) {

				// Create queue and load existing Jobs
				JobQueue queue = new JobQueue(future);
				for (Job job = this.jobQueue.dequeue(); job != null; job = this.jobQueue
						.dequeue()) {
					queue.enqueue(job);
				}

				// Use queue for Process Future
				this.jobQueue = queue;

				// Wait until the Team instance available or Process complete
				while ((this.instance == null) && (!future.isComplete())) {
					try {
						this.wait(100);
					} catch (InterruptedException ex) {
						// Continue waiting
					}
				}

				// Create the executor for the Jobs
				executor = new JobExecutor(this.instance);
			}

			// Execute Jobs until Process complete
			while (!future.isComplete()) {
				Job job = this.jobQueue.dequeue(100);
				if (job != null) {
					executor.doJob(job);
				}
			}
		}
	}

	/**
	 * Executes {@link Job} instances.
	 */
	private static class JobExecutor implements JobContext {

		/**
		 * {@link ProcessContextTeam}.
		 */
		private final ProcessContextTeam instance;

		/**
		 * Cached time.
		 */
		private long time = -1;

		/**
		 * Initiate.
		 * 
		 * @param instance
		 *            {@link ProcessContextTeam}.
		 */
		public JobExecutor(ProcessContextTeam instance) {
			this.instance = instance;
		}

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
			return this.instance.isContinueExecution;
		}
	}

}