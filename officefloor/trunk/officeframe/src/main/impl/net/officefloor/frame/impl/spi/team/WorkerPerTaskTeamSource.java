/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.frame.impl.spi.team;

import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;
import net.officefloor.frame.spi.team.source.impl.AbstractTeamSource;

/**
 * {@link TeamSource} for the {@link WorkerPerTaskTeam}.
 *
 * @author Daniel Sagenschneider
 */
public class WorkerPerTaskTeamSource extends AbstractTeamSource {

	/**
	 * Name of property for the {@link Team} name.
	 */
	public static final String TEAM_NAME_PROPERTY_NAME = "name";

	/*
	 * ==================== AbstractTeamSource ===============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(TEAM_NAME_PROPERTY_NAME, "Team name");
	}

	@Override
	protected Team createTeam(TeamSourceContext context) throws Exception {
		// Obtain the team name
		String teamName = context.getProperty(TEAM_NAME_PROPERTY_NAME,
				WorkerPerTaskTeam.class.getSimpleName());

		// Create and return the team
		return new WorkerPerTaskTeam(teamName);
	}

}