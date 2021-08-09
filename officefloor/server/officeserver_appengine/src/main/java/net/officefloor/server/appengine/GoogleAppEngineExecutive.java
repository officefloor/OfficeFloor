/*-
 * #%L
 * Google AppEngine OfficeFloor Server
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

package net.officefloor.server.appengine;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.google.appengine.api.ThreadManager;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;

import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ExecutiveContext;
import net.officefloor.frame.api.executive.ExecutiveOfficeContext;
import net.officefloor.frame.api.executive.ExecutiveStartContext;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.executive.TeamOversight;
import net.officefloor.frame.api.executive.source.ExecutiveSourceContext;
import net.officefloor.frame.api.executive.source.impl.AbstractExecutiveSource;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.TeamOverloadException;
import net.officefloor.frame.internal.structure.OfficeManager;

/**
 * {@link Executive} for Google AppEngine.
 * 
 * @author Daniel Sagenschneider
 */
public class GoogleAppEngineExecutive extends AbstractExecutiveSource
		implements Executive, ExecutionStrategy, TeamOversight {

	/**
	 * {@link ThreadFactory}.
	 */
	private ThreadFactory threadFactory;

	/*
	 * ===================== ExecutiveSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public Executive createExecutive(ExecutiveSourceContext context) throws Exception {
		this.threadFactory = context.createThreadFactory("AppEngine", this);
		return this;
	}

	/*
	 * ======================== Executive ============================
	 */

	@Override
	public Thread createThread(String threadName, ThreadGroup threadGroup, Runnable runnable) {
		Environment environment = ApiProxy.getCurrentEnvironment();
		if (environment != null) {
			// Running within AppEngine environment
			return ThreadManager.createThreadForCurrentRequest(runnable);
		} else {
			// Running outside AppEngine
			return new Thread(threadGroup, runnable, threadName);
		}
	}

	@Override
	public ExecutionStrategy[] getExcutionStrategies() {
		return new ExecutionStrategy[] { this };
	}

	@Override
	public TeamOversight getTeamOversight() {
		return this;
	}

	@Override
	public void startManaging(ExecutiveStartContext context) throws Exception {
		// Request scoped, so nothing to start
	}

	@Override
	public ProcessIdentifier createProcessIdentifier(ExecutiveOfficeContext officeContext) {
		OfficeManager officeManager = officeContext.hireOfficeManager();
		AppEngineProcessIdentifier processIdentifier = new AppEngineProcessIdentifier(officeManager, this.threadFactory);
		return processIdentifier;
	}

	@Override
	public OfficeManager getOfficeManager(ProcessIdentifier processIdentifier, OfficeManager defaultOfficeManager) {
		AppEngineProcessIdentifier appEngineId = (AppEngineProcessIdentifier) processIdentifier;
		return appEngineId.officeManager;
	}

	@Override
	public Executor createExecutor(ProcessIdentifier processIdentifier) {
		AppEngineProcessIdentifier appEngineId = (AppEngineProcessIdentifier) processIdentifier;
		return appEngineId.executor;
	}

	@Override
	public void schedule(ProcessIdentifier processIdentifier, long delay, Runnable runnable) {
		AppEngineProcessIdentifier appEngineId = (AppEngineProcessIdentifier) processIdentifier;
		appEngineId.scheduler.schedule(runnable, delay, TimeUnit.MILLISECONDS);
	}

	@Override
	public void processComplete(ProcessIdentifier processIdentifier) {
		AppEngineProcessIdentifier appEngineId = (AppEngineProcessIdentifier) processIdentifier;

		// Shutdown
		appEngineId.executor.shutdown();
		appEngineId.scheduler.shutdown();

		// Ensure shutdown
		try {
			appEngineId.executor.awaitTermination(10, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			// Ignore interrupt and carry on
		}
		try {
			appEngineId.scheduler.awaitTermination(10, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			// Ignore interrupt and carry on
		}
	}

	@Override
	public void stopManaging() throws Exception {
		// Request scoped, so nothing to stop
	}

	/*
	 * ======================== ExecutionStrategy =======================
	 */

	@Override
	public String getExecutionStrategyName() {
		return "APPENGINE";
	}

	@Override
	public ThreadFactory[] getThreadFactories() {
		return new ThreadFactory[] { this.threadFactory };
	}

	/*
	 * ========================== TeamOversight =========================
	 */

	@Override
	public Team createTeam(ExecutiveContext context) throws Exception {

		// Allow requesting no oversight
		if (context.isRequestNoTeamOversight()) {
			return context.getTeamSource().createTeam(context);
		}

		// Provide team delegating to request thread
		return new Team() {

			@Override
			public void startWorking() {
				// Nothing to start, as request scoped
			}

			@Override
			public void assignJob(Job job) throws TeamOverloadException, Exception {
				AppEngineProcessIdentifier appEngineId = (AppEngineProcessIdentifier) job.getProcessIdentifier();

				// Delegate to request scoped executor
				try {
					appEngineId.executor.execute(job);
				} catch (RejectedExecutionException ex) {
					// Determine if due to shutdown
					if (appEngineId.executor.isShutdown()) {
						// Undertake with this thread
						job.run();
					} else {
						// Not shutdown, so propagate
						throw ex;
					}
				}
			}

			@Override
			public void stopWorking() {
				// Nothing to stop, as request scoped
			}
		};
	}

	/**
	 * AppEngine {@link ProcessIdentifier}.
	 */
	private static class AppEngineProcessIdentifier implements ProcessIdentifier {

		/**
		 * {@link OfficeManager}.
		 */
		private final OfficeManager officeManager;

		/**
		 * {@link ExecutorService}.
		 */
		private final ExecutorService executor;

		/**
		 * {@link ScheduledExecutorService}.
		 */
		private final ScheduledExecutorService scheduler;

		/**
		 * Instantiate.
		 * 
		 * @param officeManager {@link OfficeManager}.
		 * @param threadFactory {@link ThreadFactory}.
		 */
		private AppEngineProcessIdentifier(OfficeManager officeManager, ThreadFactory threadFactory) {
			this.officeManager = officeManager;
			this.executor = Executors.newCachedThreadPool(threadFactory);
			this.scheduler = Executors.newScheduledThreadPool(1, threadFactory);
		}
	}

}
