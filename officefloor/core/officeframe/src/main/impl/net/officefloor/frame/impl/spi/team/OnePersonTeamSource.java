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

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.impl.AbstractTeamSource;

/**
 * {@link TeamSource} for the {@link OnePersonTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public class OnePersonTeamSource extends AbstractTeamSource {

	/**
	 * Property name of the max wait time in milliseconds.
	 */
	public static final String MAX_WAIT_TIME_PROPERTY_NAME = "wait";

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

		// Obtain the team name
		String teamName = context.getTeamName();

		// Return the one person team
		return new OnePersonTeam(teamName, waitTime);
	}

}