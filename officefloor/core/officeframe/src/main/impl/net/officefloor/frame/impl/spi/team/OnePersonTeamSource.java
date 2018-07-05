/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.spi.team;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;
import net.officefloor.frame.util.TeamSourceStandAlone;

/**
 * {@link TeamSource} for the {@link OnePersonTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class OnePersonTeamSource extends AbstractTeamSource {

	/**
	 * Property to specify the worker {@link Thread} priority.
	 */
	public static final String PROPERTY_THREAD_PRIORITY = "person.thread.priority";

	/**
	 * Property name of the max wait time in milliseconds.
	 */
	public static final String MAX_WAIT_TIME_PROPERTY_NAME = "wait";

	/**
	 * Default {@link Thread} priority.
	 */
	public static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY;

	/**
	 * Convenience method to create a {@link OnePersonTeam}.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @return {@link OnePersonTeam}.
	 * @throws Exception
	 *             If fails to create the {@link OnePersonTeam}.
	 */
	public static OnePersonTeam createOnePersonTeam(String teamName) throws Exception {
		return (OnePersonTeam) new TeamSourceStandAlone(teamName).loadTeam(OnePersonTeamSource.class);
	}

	/*
	 * ==================== AbstractTeamSource ==============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(MAX_WAIT_TIME_PROPERTY_NAME, "Wait time (ms)");
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {

		// Obtain the wait time
		long waitTime = Long.parseLong(context.getProperty(MAX_WAIT_TIME_PROPERTY_NAME, "100"));

		// Obtain the thread priority
		int priority = Integer
				.valueOf(context.getProperty(PROPERTY_THREAD_PRIORITY, String.valueOf(DEFAULT_THREAD_PRIORITY)));

		// Return the one person team
		ThreadFactory threadFactory = context.getThreadFactory(priority);
		return new OnePersonTeam(threadFactory, waitTime);
	}

}