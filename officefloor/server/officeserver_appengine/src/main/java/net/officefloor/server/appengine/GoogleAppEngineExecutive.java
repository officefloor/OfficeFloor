/*-
 * #%L
 * Google AppEngine OfficeFloor Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
