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

package net.officefloor.frame.stress;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveContext;
import net.officefloor.frame.api.executive.ExecutiveOfficeContext;
import net.officefloor.frame.api.executive.ExecutiveStartContext;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.TeamOverloadException;
import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.frame.internal.structure.OfficeManager;

/**
 * Request scoped {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public class RequestScopedExecutive extends AbstractExecutiveSource implements Executive, TeamOversight {

	private ThreadFactory threadFactory;

	/*
	 * ==================== ExecutiveSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
		this.threadFactory = context.createThreadFactory("default", this);
		return this;
	}

	/*
	 * ======================= Executive ================================
	 */

	@Override
	public Thread createThread(String threadName, ThreadGroup threadGroup, Runnable runnable) {
		return new Thread(threadGroup, runnable, threadName);
	}

	@Override
	public ExecutionStrategy[] getExcutionStrategies() {
		return new ExecutionStrategy[] { new ExecutionStrategy() {
			@Override
			public String getExecutionStrategyName() {
				return "default";
			}

			@Override
			public ThreadFactory[] getThreadFactories() {
				return new ThreadFactory[] { RequestScopedExecutive.this.threadFactory };
			}
		} };
	}

	@Override
	public TeamOversight getTeamOversight() {
		return this;
	}

	@Override
	public <T extends Throwable> ProcessManager manageExecution(Execution<T> execution) throws T {
		return execution.execute();
	}

	@Override
	public void startManaging(ExecutiveStartContext context) throws Exception {
		// Nothing to start, as request scoped
	}

	@Override
	public ProcessIdentifier createProcessIdentifier(ExecutiveOfficeContext officeContext) {
		return new RequestScopedProcessIdentifier(officeContext.hireOfficeManager(), this.threadFactory);
	}

	@Override
	public OfficeManager getOfficeManager(ProcessIdentifier processIdentifier, OfficeManager defaultOfficeManager) {
		RequestScopedProcessIdentifier requestScoped = (RequestScopedProcessIdentifier) processIdentifier;
		return requestScoped.officeManager;
	}

	@Override
	public Executor createExecutor(ProcessIdentifier processIdentifier) {
		RequestScopedProcessIdentifier requestScoped = (RequestScopedProcessIdentifier) processIdentifier;
		return requestScoped.executorService;
	}

	@Override
	public void schedule(ProcessIdentifier processIdentifier, long delay, Runnable runnable) {
		RequestScopedProcessIdentifier requestScoped = (RequestScopedProcessIdentifier) processIdentifier;
		requestScoped.scheduledService.schedule(runnable, delay, TimeUnit.MILLISECONDS);
	}

	@Override
	public void processComplete(ProcessIdentifier processIdentifier) {
		RequestScopedProcessIdentifier requestScoped = (RequestScopedProcessIdentifier) processIdentifier;

		// Stop the executors
		requestScoped.executorService.shutdown();
		requestScoped.scheduledService.shutdown();

		// Await their termination (should be no further as request complete)
		try {
			requestScoped.executorService.awaitTermination(10, TimeUnit.MICROSECONDS);
		} catch (InterruptedException ex) {
			// Ignore interruption and carry on
		}
		try {
			requestScoped.scheduledService.awaitTermination(10, TimeUnit.MICROSECONDS);
		} catch (InterruptedException ex) {
			// Ignore interruption and carry on
		}
	}

	@Override
	public void stopManaging() throws Exception {
		// Nothing to stop, as request scoped
	}

	/*
	 * =========================== TeamOversight =================================
	 */

	@Override
	public Team createTeam(ExecutiveContext context) throws Exception {

		// Respect no oversight
		if (context.isRequestNoTeamOversight()) {
			return context.getTeamSource().createTeam(context);
		}

		// Use process bound executor
		return new Team() {

			@Override
			public void startWorking() {
				// Using request scoped executor
			}

			@Override
			public void assignJob(Job job) throws TeamOverloadException, Exception {
				RequestScopedProcessIdentifier requestScoped = (RequestScopedProcessIdentifier) job
						.getProcessIdentifier();
				try {
					requestScoped.executorService.execute(job);
				} catch (RejectedExecutionException ex) {
					// Determine if due to shutting down
					if (requestScoped.executorService.isShutdown()) {
						// Undertake on current thread
						job.run();
					} else {
						// Propagate the failure
						throw ex;
					}
				}
			}

			@Override
			public void stopWorking() {
				// Using request scoped executor
			}
		};
	}

	/**
	 * Request scoped {@link ProcessIdentifier}.
	 */
	private static class RequestScopedProcessIdentifier implements ProcessIdentifier {

		/**
		 * {@link OfficeManager}.
		 */
		private final OfficeManager officeManager;

		/**
		 * {@link ExecutorService}.
		 */
		private final ExecutorService executorService;

		/**
		 * {@link ScheduledExecutorService}.
		 */
		private final ScheduledExecutorService scheduledService;

		/**
		 * Instantiate.
		 * 
		 * @param officeManager {@link OfficeManager}.
		 * @param threadFactory {@link ThreadFactory};
		 */
		private RequestScopedProcessIdentifier(OfficeManager officeManager, ThreadFactory threadFactory) {
			this.officeManager = officeManager;
			this.executorService = Executors.newCachedThreadPool(threadFactory);
			this.scheduledService = Executors.newScheduledThreadPool(1, threadFactory);
		}
	}

}
