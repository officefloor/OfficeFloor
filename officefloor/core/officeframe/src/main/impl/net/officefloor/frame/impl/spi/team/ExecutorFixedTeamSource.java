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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;

/**
 * {@link TeamSource} utilising a fixed {@link ExecutorService}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutorFixedTeamSource extends AbstractExecutorTeamSource {

	/*
	 * ===================== AbstractExecutorTeamSource =====================
	 */

	@Override
	protected ExecutorServiceFactory createExecutorServiceFactory(TeamSourceContext context,
			final ThreadFactory threadFactory) throws Exception {

		// Obtain the team details
		final int teamSize = context.getTeamSize();
		if (teamSize < 1) {
			throw new IllegalArgumentException("Team size must be one or more");
		}

		// Create and return the factory
		return new FixedExecutorServiceFactory(teamSize, threadFactory);
	}

	/**
	 * {@link ExecutorServiceFactory} for a fixed size.
	 */
	private static class FixedExecutorServiceFactory implements ExecutorServiceFactory {

		/**
		 * Size of the {@link Team}.
		 */
		private final int teamSize;

		/**
		 * {@link ThreadFactory}.
		 */
		private final ThreadFactory threadFactory;

		/**
		 * Initiate.
		 * 
		 * @param teamSize      Size of the {@link Team}.
		 * @param threadFactory {@link ThreadFactory}.
		 */
		public FixedExecutorServiceFactory(int teamSize, ThreadFactory threadFactory) {
			this.teamSize = teamSize;
			this.threadFactory = threadFactory;
		}

		/*
		 * ================== ExecutorServiceFactory ========================
		 */

		@Override
		public ExecutorService createExecutorService() {
			return Executors.newFixedThreadPool(this.teamSize, this.threadFactory);
		}
	}

}
