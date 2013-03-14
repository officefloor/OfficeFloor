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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;
import net.officefloor.frame.spi.team.source.impl.AbstractTeamSource;

/**
 * {@link TeamSource} based on the {@link Executors} cached thread pool.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractExecutorTeamSource extends AbstractTeamSource {

	/**
	 * Name of property to obtain the {@link Thread} priority.
	 */
	public static final String PROPERTY_THREAD_PRIORITY = "thread.priority";

	/**
	 * Obtains the factory to create {@link ExecutorService}.
	 * 
	 * @param context
	 *            {@link TeamSourceContext}.
	 * @param threadFactory
	 *            {@link ThreadFactory} to use for the creation of the
	 *            {@link Thread} instances.
	 * @return {@link ExecutorServiceFactory}.
	 */
	protected abstract ExecutorServiceFactory createExecutorServiceFactory(
			TeamSourceContext context, ThreadFactory threadFactory)
			throws Exception;

	/**
	 * Factory to create the {@link ExecutorService}.
	 */
	protected static interface ExecutorServiceFactory {

		/**
		 * Creates the {@link ExecutorService}.
		 * 
		 * @return {@link ExecutorService}.
		 */
		ExecutorService createExecutorService();

	}

	/**
	 * Creates the {@link Team}.
	 * 
	 * @param executorServiceFactory
	 *            {@link ExecutorServiceFactory}.
	 * @param teamIdentifier
	 *            {@link TeamIdentifier}.
	 * @return {@link Team}.
	 */
	protected static Team createTeam(
			ExecutorServiceFactory executorServiceFactory,
			TeamIdentifier teamIdentifier) {

		// Create and return the executor team
		return new ExecutorTeam(executorServiceFactory, teamIdentifier);
	}

	/*
	 * ======================= TeamSource ==================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required properties
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {

		// Obtain the details of the team
		String teamName = context.getTeamName();
		TeamIdentifier teamIdentifier = context.getTeamIdentifier();
		final int threadPriority = Integer
				.valueOf(context.getProperty(PROPERTY_THREAD_PRIORITY,
						String.valueOf(Thread.NORM_PRIORITY)));

		// Create and return the executor team
		return new ExecutorTeam(this.createExecutorServiceFactory(context,
				new TeamThreadFactory(teamName, threadPriority)),
				teamIdentifier);
	}

	/**
	 * {@link ThreadFactory} for the {@link Team}.
	 */
	protected static class TeamThreadFactory implements ThreadFactory {

		/**
		 * {@link ThreadGroup}.
		 */
		private final ThreadGroup group;

		/**
		 * Prefix of {@link Thread} name.
		 */
		private final String threadNamePrefix;

		/**
		 * Index of the next {@link Thread}.
		 */
		private final AtomicInteger nextThreadIndex = new AtomicInteger(1);

		/**
		 * {@link Thread} priority.
		 */
		private final int threadPriority;

		/**
		 * Initiate.
		 * 
		 * @param teamName
		 *            Name of the {@link Team}.
		 * @param threadPriority
		 *            {@link Thread} priority.
		 */
		protected TeamThreadFactory(String teamName, int threadPriority) {
			SecurityManager s = System.getSecurityManager();
			this.group = (s != null) ? s.getThreadGroup() : Thread
					.currentThread().getThreadGroup();
			this.threadNamePrefix = teamName + "-";
			this.threadPriority = threadPriority;
		}

		/*
		 * ==================== ThreadFactory =======================
		 */

		@Override
		public Thread newThread(Runnable r) {

			// Create and configure the thread
			Thread thread = new Thread(this.group, r, this.threadNamePrefix
					+ this.nextThreadIndex.getAndIncrement(), 0);
			if (thread.isDaemon()) {
				thread.setDaemon(false);
			}
			if (thread.getPriority() != this.threadPriority) {
				thread.setPriority(this.threadPriority);
			}

			// Return the thread
			return thread;
		}
	}

	/**
	 * {@link Team} based on the {@link ExecutorService}.
	 */
	public static class ExecutorTeam implements Team {

		/**
		 * {@link ExecutorServiceFactory}.
		 */
		private final ExecutorServiceFactory factory;

		/**
		 * {@link TeamIdentifier} of this {@link Team}.
		 */
		private final TeamIdentifier teamIdentifier;

		/**
		 * {@link ExecutorService}.
		 */
		private ExecutorService servicer;

		/**
		 * Indicates if to continue working.
		 */
		private volatile boolean isContinueWorking = true;

		/**
		 * Initiate.
		 * 
		 * @param factory
		 *            {@link ExecutorServiceFactory}.
		 */
		public ExecutorTeam(ExecutorServiceFactory factory,
				TeamIdentifier teamIdentifier) {
			this.factory = factory;
			this.teamIdentifier = teamIdentifier;
		}

		/*
		 * ==================== Team ============================
		 */

		@Override
		public synchronized void startWorking() {
			this.servicer = this.factory.createExecutorService();
		}

		@Override
		public void assignJob(Job job, TeamIdentifier assignerTeam) {
			this.servicer.execute(new JobRunnable(job, this));
		}

		@Override
		public synchronized void stopWorking() {

			// Flag to stop working
			this.isContinueWorking = false;

			// Shutdown servicer
			this.servicer.shutdown();
			this.servicer = null;
		}
	}

	/**
	 * {@link Runnable} {@link JobContext}.
	 */
	private static class JobRunnable implements Runnable, JobContext {

		/**
		 * {@link Job} to execute.
		 */
		private final Job job;

		/**
		 * {@link ExecutorTeam} responsible for the {@link Job} completion.
		 */
		private final ExecutorTeam team;

		/**
		 * Time optimisation.
		 */
		private long time = -1;

		/**
		 * Initiate.
		 * 
		 * @param job
		 *            {@link Job} to execute.
		 * @param team
		 *            {@link ExecutorTeam} responsible for the {@link Job}
		 *            completion.
		 */
		public JobRunnable(Job job, ExecutorTeam team) {
			this.job = job;
			this.team = team;
		}

		/*
		 * ================= Runnable ======================
		 */

		@Override
		public void run() {
			do {

				// Attempt to complete the Job.
				if (this.job.doJob(this)) {
					// Job complete
					return;
				}

			} while (this.team.isContinueWorking);
		}

		/*
		 * ================= JobContext ===================
		 */

		@Override
		public long getTime() {
			if (time < 0) {
				time = System.currentTimeMillis();
			}
			return time;
		}

		@Override
		public TeamIdentifier getCurrentTeam() {
			return this.team.teamIdentifier;
		}

		@Override
		public boolean continueExecution() {
			// Always continue execution
			return true;
		}
	}

}