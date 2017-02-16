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

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.util.TeamSourceStandAlone;

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
	 * Convenience method to create a {@link Team} from the implementation of
	 * this {@link AbstractExecutorTeamSource}.
	 * 
	 * @return {@link Team}.
	 * @throws Exception
	 *             If fails to load {@link Team}.
	 */
	public Team createTeam(String... parameterNameValues) throws Exception {
		TeamSourceStandAlone standAlone = new TeamSourceStandAlone();
		for (int i = 0; i < parameterNameValues.length; i += 2) {
			String name = parameterNameValues[i];
			String value = parameterNameValues[i + 1];
			standAlone.addProperty(name, value);
		}
		return standAlone.loadTeam(this.getClass());
	}

	/**
	 * Obtains the factory to create {@link ExecutorService}.
	 * 
	 * @param context
	 *            {@link TeamSourceContext}.
	 * @param threadFactory
	 *            {@link ThreadFactory} to use for the creation of the
	 *            {@link Thread} instances.
	 * @return {@link ExecutorServiceFactory}.
	 * @throws Exception
	 *             If fails to create the {@link ExecutorServiceFactory}.
	 */
	protected abstract ExecutorServiceFactory createExecutorServiceFactory(TeamSourceContext context,
			ThreadFactory threadFactory) throws Exception;

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
		final int threadPriority = Integer
				.valueOf(context.getProperty(PROPERTY_THREAD_PRIORITY, String.valueOf(Thread.NORM_PRIORITY)));

		// Create and return the executor team
		return new ExecutorTeam(
				this.createExecutorServiceFactory(context, new TeamThreadFactory(teamName, threadPriority)));
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
			this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			this.threadNamePrefix = teamName + "-";
			this.threadPriority = threadPriority;
		}

		/*
		 * ==================== ThreadFactory =======================
		 */

		@Override
		public Thread newThread(Runnable r) {

			// Create and configure the thread
			Thread thread = new Thread(this.group, r, this.threadNamePrefix + this.nextThreadIndex.getAndIncrement(),
					0);
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
		 * {@link ExecutorService}.
		 */
		private ExecutorService servicer;

		/**
		 * Initiate.
		 * 
		 * @param factory
		 *            {@link ExecutorServiceFactory}.
		 */
		public ExecutorTeam(ExecutorServiceFactory factory) {
			this.factory = factory;
		}

		/*
		 * ==================== Team ============================
		 */

		@Override
		public void startWorking() {
			this.servicer = this.factory.createExecutorService();
		}

		@Override
		public void assignJob(Job job) {
			this.servicer.execute(job);
		}

		@Override
		public void stopWorking() {
			// Shutdown servicer
			this.servicer.shutdown();
			this.servicer = null;
		}
	}

}