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
package net.officefloor.frame.test;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.api.team.source.TeamSourceContext;
import net.officefloor.frame.api.team.source.TeamSourceSpecification;
import net.officefloor.frame.impl.spi.team.OnePersonTeam;
import net.officefloor.frame.impl.spi.team.WorkerPerJobTeam;

/**
 * Mock {@link TeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
@TestSource
public class MockTeamSource implements TeamSource {

	/**
	 * Convenience method to create a {@link OnePersonTeam}.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @return {@link OnePersonTeam}.
	 */
	public static OnePersonTeam createOnePersonTeam(String teamName) {
		return new OnePersonTeam(teamName, 100);
	}

	/**
	 * Convenience method to create a {@link WorkerPerJobTeam}.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @return {@link WorkerPerJobTeam}
	 */
	public static WorkerPerJobTeam createWorkerPerTaskTeam(String teamName) {
		return new WorkerPerJobTeam(teamName);
	}

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
	public static TeamBuilder<?> bindTeamBuilder(OfficeFloorBuilder officeFloorBuilder, String teamName, Team team) {

		// Create the team builder
		TeamBuilder<?> teamBuilder = officeFloorBuilder.addTeam(teamName, MockTeamSource.class);

		// Bind team builder to team
		teamBuilder.addProperty(TEAM_PROPERTY, teamName);
		REGISTRY.put(teamName, team);

		// Return the team builder
		return teamBuilder;
	}

	/*
	 * ======================= TeamSource ===================================
	 */

	@Override
	public TeamSourceSpecification getSpecification() {
		// No specification
		return null;
	}

	@Override
	public Team createTeam(TeamSourceContext context) throws Exception {

		// Obtain the team
		String teamName = context.getProperty(TEAM_PROPERTY);
		Team team = REGISTRY.get(teamName);

		// Return the team
		return team;
	}

}