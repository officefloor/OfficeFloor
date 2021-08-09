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
