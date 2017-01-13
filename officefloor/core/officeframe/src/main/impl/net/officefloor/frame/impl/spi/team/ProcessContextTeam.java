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

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.ProcessContextListener;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * {@link Team} to re-use the invoking {@link Thread} of the
 * {@link ProcessState}. Typically this will be the {@link Thread} invoking the
 * {@link ManagedFunction} via the {@link FunctionManager}.
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
	 * Wrap invoking {@link ManagedFunction} on the {@link FunctionManager}.
	 * This method blocks until the invoked {@link ProcessState} of the invoked
	 * {@link ManagedFunction} is complete.
	 * 
	 * @param functionManager
	 *            {@link FunctionManager} managing the {@link ManagedFunction}
	 *            to invoked.
	 * @param parameter
	 *            Parameter for the {@link ManagedFunction}.
	 * @throws InvalidParameterTypeException
	 *             Should the parameter type be invalid for the
	 *             {@link ManagedFunction}.
	 * @throws InterruptedException
	 *             Should this blocking call be interrupted.
	 */
	public static void doFunction(final FunctionManager functionManager, final Object parameter)
			throws InvalidParameterTypeException, InterruptedException {
		doProcess(new InvokeProcessState() {
			@Override
			public void invokeProcessState(FlowCallback callback) throws InvalidParameterTypeException {
				functionManager.invokeProcess(parameter, callback);
			}
		});
	}

	/**
	 * Wrap invoking {@link ProcessState} on the
	 * {@link ManagedObjectExecuteContext}. This method blocks until the invoked
	 * {@link ProcessState} is complete.
	 * 
	 * @param <F>
	 *            Flow key type.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 * @param flowKey
	 *            {@link Flow} key.
	 * @param parameter
	 *            Parameter for the initial {@link ManagedFunction}.
	 * @param managedObject
	 *            {@link ManagedObject}.
	 * @throws InterruptedException
	 *             Should this blocking call be interrupted.
	 */
	public static <F extends Enum<F>> void doProcess(ManagedObjectExecuteContext<F> executeContext, F flowKey,
			Object parameter, ManagedObject managedObject) throws InterruptedException {
		doProcess(executeContext, flowKey.ordinal(), parameter, managedObject);
	}

	/**
	 * Wrap invoking {@link ProcessState} on the
	 * {@link ManagedObjectExecuteContext}. This method blocks until the invoked
	 * {@link ProcessState} is complete.
	 * 
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 * @param flowIndex
	 *            {@link Flow} index.
	 * @param parameter
	 *            Parameter for the initial {@link ManagedFunction}.
	 * @param managedObject
	 *            {@link ManagedObject}.
	 * @throws InterruptedException
	 *             Should this blocking call be interrupted.
	 */
	public static void doProcess(final ManagedObjectExecuteContext<?> executeContext, final int flowIndex,
			final Object parameter, final ManagedObject managedObject) throws InterruptedException {
		try {
			doProcess(new InvokeProcessState() {
				@Override
				public void invokeProcessState(FlowCallback calback) {
					executeContext.invokeProcess(flowIndex, parameter, managedObject, 0, calback);
				}
			});
		} catch (InvalidParameterTypeException ex) {
			throw new IllegalStateException("Should not be able to throw this exception", ex);
		}
	}

	/**
	 * Interface to wrap invoking the {@link ProcessState}.
	 */
	private static interface InvokeProcessState {

		/**
		 * Invokes the {@link ProcessState}.
		 * 
		 * @param calback
		 *            {@link FlowCallback}.
		 * @throws InvalidParameterTypeException
		 *             Should the parameter type be invalid for
		 *             {@link ManagedFunction}.
		 */
		void invokeProcessState(FlowCallback calback) throws InvalidParameterTypeException;
	}

	/**
	 * <p>
	 * Wrap invoking {@link ProcessState} on the {@link InvokeProcessState} to
	 * allow the {@link Thread} to be available to execute the
	 * {@link ManagedFunction} instances of the {@link ProcessState}.
	 * <p>
	 * This method blocks until the invoked {@link ProcessState} is complete.
	 * 
	 * @param invoker
	 *            {@link InvokeProcessState}.
	 * @throws InvalidParameterTypeException
	 *             Should the parameter type be invalid for the
	 *             {@link ManagedFunction}.
	 * @throws InterruptedException
	 *             Should this blocking call be interrupted.
	 */
	private static void doProcess(InvokeProcessState invoker)
			throws InvalidParameterTypeException, InterruptedException {

		// Obtain the current Thread
		final Thread currentThread = Thread.currentThread();
		try {

			// Register the Job Queue Executor for the Thread.
			// Must be done before invoking task to ensure available.
			JobQueueExecutor executor = new JobQueueExecutor();
			synchronized (threadToExecutor) {
				threadToExecutor.put(currentThread, executor);
			}

			// Flow callback for completion
			FlowCallback callback = new FlowCallback() {
				@Override
				public void run(Throwable escalation) throws Throwable {
					threadToExecutor.remove(currentThread);
					executor.isProcessComplete = true;
				}
			};

			// Invoke the process
			invoker.invokeProcessState(callback);

			// Blocking call to execute the Jobs
			executor.executeJobs();

		} finally {
			// Ensure unregister current Thread (no further Jobs registered)
			JobQueueExecutor executor;
			synchronized (threadToExecutor) {
				executor = threadToExecutor.remove(currentThread);
			}

			// Jobs can exist for another invoked Process (eg recycling)
			if (executor != null) {

				// Determine if Jobs to complete
				Job job = executor.jobQueue.dequeue();
				if (job != null) {
					// Complete execution of existing Jobs
					JobExecutor completionExecutor = new JobExecutor(executor.instance);
					while (job != null) {

						// Complete the Job
						completionExecutor.doJob(job);

						// Obtain potential next Job to complete
						job = executor.jobQueue.dequeue();
					}
				}
			}
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
		 * Flag indicating if the {@link ProcessState} is complete.
		 */
		private volatile boolean isProcessComplete = false;

		/**
		 * {@link JobQueue}.
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
		 * {@link ProcessState}.
		 */
		public void executeJobs() {

			// Ensure coordinate with assigning Jobs
			JobExecutor executor;
			synchronized (this) {

				// Create queue and load existing Jobs
				JobQueue queue = new JobQueue();
				for (Job job = this.jobQueue.dequeue(); job != null; job = this.jobQueue.dequeue()) {
					queue.enqueue(job);
				}

				// Use queue for Process Future
				this.jobQueue = queue;

				// Wait until the Team instance available or Process complete
				while ((this.instance == null) && (!this.isProcessComplete)) {
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
			while (!this.isProcessComplete) {
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
	private static class JobExecutor {

		/**
		 * {@link ProcessContextTeam}.
		 */
		private final ProcessContextTeam instance;

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
			job.run();
		}
	}

}