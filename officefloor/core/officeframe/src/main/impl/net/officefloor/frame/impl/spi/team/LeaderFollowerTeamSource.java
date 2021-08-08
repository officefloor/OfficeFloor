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

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;

/**
 * {@link TeamSource} for a {@link LeaderFollowerTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class LeaderFollowerTeamSource extends AbstractTeamSource {

	/**
	 * Property to specify the worker {@link Thread} priority.
	 */
	public static final String PROPERTY_THREAD_PRIORITY = "person.thread.priority";

	/**
	 * Default {@link Thread} priority.
	 */
	public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;

	/*
	 * =================== AbstractTeamSource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {

		// Obtain the required configuration
		int teamSize = context.getTeamSize();
		if (teamSize < 1) {
			throw new IllegalArgumentException("Team size must be one or more");
		}

		// Obtain the optional configuration
		long waitTime = Long.parseLong(context.getProperty("wait.time", "100"));

		// Obtain the thread priority
		int priority = Integer
				.valueOf(context.getProperty(PROPERTY_THREAD_PRIORITY, String.valueOf(DEFAULT_THREAD_PRIORITY)));

		// Create and return the team
		ThreadFactory threadFactory = context.getThreadFactory();
		if (priority != DEFAULT_THREAD_PRIORITY) {
			final ThreadFactory delegate = threadFactory;
			threadFactory = (runnable) -> {
				Thread thread = delegate.newThread(runnable);
				thread.setPriority(priority);
				return thread;
			};
		}
		return new LeaderFollowerTeam(teamSize, threadFactory, waitTime);
	}

}
