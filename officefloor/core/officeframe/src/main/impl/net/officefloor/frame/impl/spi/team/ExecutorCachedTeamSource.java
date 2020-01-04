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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;

/**
 * {@link TeamSource} utilising a cached {@link ExecutorService}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutorCachedTeamSource extends AbstractExecutorTeamSource {

	/**
	 * Property name for the minimum size.
	 */
	public static final String PROPERTY_MIN_SIZE = "minimum.size";

	/**
	 * Property name for the wait time for a {@link Job} before shutting down the
	 * {@link Thread}.
	 */
	public static final String PROPERTY_WAIT_TIME = "wait.time";

	/*
	 * ===================== AbstractExecutorTeamSource =====================
	 */

	@Override
	protected ExecutorServiceFactory createExecutorServiceFactory(TeamSourceContext context,
			final ThreadFactory threadFactory) throws Exception {

		// Obtain the configuration
		int minimumSize = Integer.valueOf(context.getProperty(PROPERTY_MIN_SIZE, String.valueOf(0)));
		int maximumSize = context.getTeamSize(Integer.MAX_VALUE);
		long waitTime = Long.valueOf(context.getProperty(PROPERTY_WAIT_TIME, String.valueOf(60_000L)));

		// Create and return the factory
		return new ExecutorServiceFactory() {
			@Override
			public ExecutorService createExecutorService() {
				return new ThreadPoolExecutor(minimumSize, maximumSize, waitTime, TimeUnit.MICROSECONDS,
						new SynchronousQueue<Runnable>(), threadFactory);
			}
		};
	}

}
