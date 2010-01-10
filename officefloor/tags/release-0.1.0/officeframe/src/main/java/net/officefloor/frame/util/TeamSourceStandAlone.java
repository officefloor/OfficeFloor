/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.frame.util;

import java.util.Properties;

import net.officefloor.frame.impl.construct.team.TeamSourceContextImpl;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceContext;

/**
 * Loads a {@link TeamSource} for stand-alone use.
 *
 * @author Daniel Sagenschneider
 */
public class TeamSourceStandAlone {

	/**
	 * {@link Properties} to initialise the {@link TeamSource}.
	 */
	private final Properties properties = new Properties();

	/**
	 * Adds a property for initialising the {@link TeamSource}.
	 *
	 * @param name
	 *            Name of property.
	 * @param value
	 *            Value of property.
	 */
	public void addProperty(String name, String value) {
		this.properties.setProperty(name, value);
	}

	/**
	 * Initialises and returns the {@link TeamSource} instance.
	 *
	 * @param teamSourceClass
	 *            {@link Class} of the {@link TeamSource}.
	 * @return Initialised {@link TeamSource}.
	 * @throws Exception
	 *             If fails instantiation and initialising the
	 *             {@link TeamSource}.
	 */
	public <TS extends TeamSource> TS loadTeamSource(Class<TS> teamSourceClass)
			throws Exception {

		TS teamSource = teamSourceClass.newInstance();

		// Initialise the team source
		TeamSourceContext context = new TeamSourceContextImpl(teamSourceClass
				.getSimpleName(), this.properties);
		teamSource.init(context);

		// Return the initialised team source
		return teamSource;
	}

	/**
	 * Returns a {@link Team} from the loaded {@link TeamSource}.
	 *
	 * @param teamSourceClass
	 *            {@link Class} of the {@link TeamSource}.
	 * @return {@link Team} from the loaded {@link TeamSource}.
	 * @throws Exception
	 *             If fails loading the {@link TeamSource} and creating a
	 *             {@link Team}.
	 */
	public <TS extends TeamSource> Team loadTeam(Class<TS> teamSourceClass)
			throws Exception {

		// Load the team source
		TS teamSource = this.loadTeamSource(teamSourceClass);

		// Return the created team
		return teamSource.createTeam();
	}

}