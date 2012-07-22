/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.JobContext;
import net.officefloor.frame.spi.team.Team;
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
	 * Obtains the factory to create {@link ExecutorService}.
	 * 
	 * @param context
	 *            {@link TeamSourceContext}.
	 * @return {@link ExecutorServiceFactory}.
	 */
	protected abstract ExecutorServiceFactory createExecutorServiceFactory(
			TeamSourceContext context) throws Exception;

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
		// No properties
	}

	@Override
	protected Team createTeam(TeamSourceContext context) throws Exception {
		return new ExecutorTeam(this.createExecutorServiceFactory(context));
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
		public synchronized void startWorking() {
			this.servicer = this.factory.createExecutorService();
		}

		@Override
		public void assignJob(Job job) {
			this.servicer.execute(new JobRunnable(job));
		}

		@Override
		public synchronized void stopWorking() {
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
		 * Time optimisation.
		 */
		private long time = -1;

		/**
		 * Initiate.
		 * 
		 * @param job
		 *            {@link Job} to execute.
		 */
		public JobRunnable(Job job) {
			this.job = job;
		}

		/*
		 * ================= Runnable ======================
		 */

		@Override
		public void run() {
			this.job.doJob(this);
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
		public boolean continueExecution() {
			// Always continue execution
			return true;
		}
	}

}