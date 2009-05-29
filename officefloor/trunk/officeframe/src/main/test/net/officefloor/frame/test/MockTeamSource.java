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
package net.officefloor.frame.test;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.spi.TestSource;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;
import net.officefloor.frame.spi.team.source.TeamSourceSpecification;

/**
 * Mock {@link TeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class MockTeamSource implements TeamSource {

	/**
	 * Property name to source the {@link Team}.
	 */
	private static final String TEAM_PROPERTY = "net.officefloor.frame.construct.team";

	/**
	 * Registry of the {@link Team} instances.
	 */
	private static final Map<String, Team> REGISTRY = new HashMap<String, Team>();

	/**
	 * Binds the {@link Team} to the name.
	 * 
	 * @param officeFloorBuilder
	 *            {@link OfficeFloorBuilder}.
	 * @param teamName
	 *            Name of {@link Team}.
	 * @param team
	 *            {@link Team}.
	 * @return {@link TeamBuilder}.
	 */
	public static TeamBuilder<?> bindTeamBuilder(
			OfficeFloorBuilder officeFloorBuilder, String teamName, Team team) {

		// Create the team builder
		TeamBuilder<?> teamBuilder = officeFloorBuilder.addTeam(teamName,
				MockTeamSource.class);

		// Bind team builder to team
		teamBuilder.addProperty(TEAM_PROPERTY, teamName);
		REGISTRY.put(teamName, team);

		// Return the team builder
		return teamBuilder;
	}

	/**
	 * {@link Team} to be returned.
	 */
	private Team team;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.source.TeamSource#getSpecification()
	 */
	@Override
	public TeamSourceSpecification getSpecification() {
		// No specification
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.frame.spi.team.TeamSource#init(net.officefloor.frame.
	 * spi.team.TeamSourceContext)
	 */
	@Override
	public void init(TeamSourceContext context) throws Exception {
		// Obtain the team
		String teamName = context.getProperty(TEAM_PROPERTY);
		this.team = REGISTRY.get(teamName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.frame.spi.team.TeamSource#createTeam()
	 */
	@Override
	public Team createTeam() {
		return this.team;
	}

}
