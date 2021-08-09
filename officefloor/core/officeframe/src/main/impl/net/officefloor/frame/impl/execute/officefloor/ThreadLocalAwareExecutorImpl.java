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

package net.officefloor.frame.impl.execute.officefloor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.impl.spi.team.JobQueue;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;

/**
 * {@link ThreadLocalAwareExecutor} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class ThreadLocalAwareExecutorImpl implements ThreadLocalAwareExecutor {

	/**
	 * Mapping of {@link Thread} to {@link JobQueueExecutor}.
	 */
	private final Map<Thread, JobQueueExecutor> threadToExecutor = new ConcurrentHashMap<>();

	/**
	 * Mapping of {@link ProcessState} identifier to {@link JobQueueExecutor}.
	 */
	private final Map<Object, JobQueueExecutor> processToExecutor = new ConcurrentHashMap<>();

	/*
	 * ================== ThreadLocalAwareExecutor =============================
	 */

	@Override
	public void runInContext(FunctionState function, FunctionLoop loop) {

		// Obtain the process
		ProcessState process = function.getThreadState().getProcessState();

		// Obtain the executor for the context thread
		Thread currentThread = Thread.currentThread();
		JobQueueExecutor executor = this.threadToExecutor.get(currentThread);
		
		// Determine if must create executor for thread
		boolean isWaitOnComplete = false;
		if (executor == null) {

			// First process for the thread
			executor = new JobQueueExecutor(currentThread);
			this.threadToExecutor.put(currentThread, executor);
			isWaitOnComplete = true;
		}
		
		// Register process to executor
		executor.registerProcess(process);
		this.processToExecutor.put(process.getProcessIdentifier(), executor);

		// Undertake the function within context
		loop.executeFunction(function);

		// Wait on completion
		if (isWaitOnComplete) {
			// Execute the jobs by the thread
			executor.executeJobs();
		}
	}

	@Override
	public void execute(Job job) {

		// Obtain the executor
		Object processIdentifier = job.getProcessIdentifier();
		JobQueueExecutor executor = this.processToExecutor.get(processIdentifier);

		// Assign the job for execution
		executor.jobQueue.enqueue(job);
	}

	@Override
	public void processComplete(ProcessState processState) {

		// Obtain the executor
		Object processIdentifier = processState.getProcessIdentifier();
		JobQueueExecutor executor = this.processToExecutor.get(processIdentifier);

		// Ignore internal processes (such as recycle managed object)
		if (executor == null) {
			return;
		}

		// Notify of complete process
		executor.processComplete(processState);
		this.processToExecutor.remove(processState);
	}

	/**
	 * Executes {@link Job} instances from the {@link JobQueue}.
	 */
	private class JobQueueExecutor {

		/**
		 * {@link JobQueue}.
		 */
		private final JobQueue jobQueue = new JobQueue(this);

		/**
		 * Registered {@link ProcessState} identifier instances. Typically should only
		 * invoke the single {@link ProcessState}.
		 */
		private final Map<Object, Object> registeredProcessIdentifiers = new ConcurrentHashMap<>();

		/**
		 * {@link Thread}.
		 */
		private final Thread thread;

		/**
		 * Flag indicating if complete.
		 */
		private volatile boolean isComplete = false;

		/**
		 * Instantiate.
		 * 
		 * @param thread {@link Thread}.
		 */
		private JobQueueExecutor(Thread thread) {
			this.thread = thread;
		}

		/**
		 * Registers another {@link ProcessState}.
		 * 
		 * @param process {@link ProcessState}.
		 */
		private void registerProcess(ProcessState process) {
			Object processIdentifier = process.getProcessIdentifier();
			this.registeredProcessIdentifiers.put(processIdentifier, processIdentifier);
		}

		/**
		 * Indicates the {@link ProcessState} has completed.
		 * 
		 * @param process {@link ProcessState} that has completed.
		 */
		private void processComplete(ProcessState process) {

			// Unregister the completed process
			Object processIdentifier = process.getProcessIdentifier();
			this.registeredProcessIdentifiers.remove(processIdentifier);

			// Indicate if complete
			if (this.registeredProcessIdentifiers.size() == 0) {
				this.isComplete = true;
				
				// Wake up job queue (allow completion)
				this.jobQueue.wakeUp();
			}
		}

		/**
		 * Blocking call to execute the {@link Job} instances until completion of all
		 * {@link ProcessState} instances registered with this {@link JobQueueExecutor}.
		 */
		public void executeJobs() {

			// Execute jobs until all processes complete
			while (!this.isComplete) {

				// Obtain the next job to execute
				Job job = this.jobQueue.dequeue(100);
				while (job != null) {

					// Execute the job
					job.run();

					// Obtain the next job
					job = this.jobQueue.dequeue(100);
				}
			}

			// Complete so unregister
			ThreadLocalAwareExecutorImpl.this.threadToExecutor.remove(this.thread);
		}
	}

}
