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

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;

/**
 * {@link TeamSource} utilising a fixed {@link ExecutorService}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutorFixedTeamSource extends AbstractExecutorTeamSource {

	/**
	 * Name of property to specify maximum number of {@link Thread} instances.
	 */
	public static final String PROPERTY_TEAM_SIZE = "team.size";

	/*
	 * ===================== AbstractExecutorTeamSource =====================
	 */

	@Override
	protected ExecutorServiceFactory createExecutorServiceFactory(TeamSourceContext context,
			final ThreadFactory threadFactory) throws Exception {

		// Obtain the team details
		final int teamSize = Integer.valueOf(context.getProperty(PROPERTY_TEAM_SIZE));

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
		 * @param teamSize
		 *            Size of the {@link Team}.
		 * @param threadFactory
		 *            {@link ThreadFactory}.
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