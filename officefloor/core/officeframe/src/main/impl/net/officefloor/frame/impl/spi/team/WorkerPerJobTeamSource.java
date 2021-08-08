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
