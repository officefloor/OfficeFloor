/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.spi.team;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;

/**
 * {@link TeamSource} for the {@link WorkerPerJobTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class WorkerPerJobTeamSource extends AbstractTeamSource {

	/**
	 * Property to specify the worker {@link Thread} priority.
	 */
	public static final String PROPERTY_THREAD_PRIORITY = "worker.thread.priority";

	/**
	 * Default {@link Thread} priority.
	 */
	public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;

	/*
	 * ==================== AbstractTeamSource ===============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {

		// Obtain the thread priority
		int priority = Integer
				.valueOf(context.getProperty(PROPERTY_THREAD_PRIORITY, String.valueOf(DEFAULT_THREAD_PRIORITY)));

		// Obtain the thread factory
		ThreadFactory threadFactory = context.getThreadFactory();
		if (priority != Thread.NORM_PRIORITY) {
			ThreadFactory delegate = threadFactory;
			threadFactory = (runnable) -> {
				Thread thread = delegate.newThread(runnable);
				thread.setPriority(priority);
				return thread;
			};
		}

		// Create and return the team
		return new WorkerPerJobTeam(threadFactory);
	}

	/**
	 * Worker per {@link Job} {@link Team}.
	 */
	private static class WorkerPerJobTeam implements Team {

		/**
		 * {@link ThreadFactory}.
		 */
		private final ThreadFactory threadFactory;

		/**
		 * Instantiate.
		 * 
		 * @param threadFactory {@link ThreadFactory}.
		 */
		public WorkerPerJobTeam(ThreadFactory threadFactory) {
			this.threadFactory = threadFactory;
		}

		/*
		 * ======================== Team ========================
		 */

		@Override
		public void startWorking() {
			// No initial workers as hired when required
		}

		@Override
		public void assignJob(Job job) {

			// Hire worker to execute the job
			Thread thread = this.threadFactory.newThread(job);
			thread.start();
		}

		@Override
		public void stopWorking() {
		}
	}

}
