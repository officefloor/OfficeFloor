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
package net.officefloor.frame.impl.execute.team;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.TeamIdentifier;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * {@link TeamManagement} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamManagementImpl implements TeamManagement {

	/**
	 * Creates a {@link TeamIdentifier}.
	 * 
	 * @return {@link TeamIdentifier}.
	 */
	public static TeamIdentifier createTeamIdentifier() {
		return new TeamIdentifier() {
		};
	}

	/**
	 * {@link TeamIdentifier} identifying the {@link Team} under this
	 * {@link TeamManagement}.
	 */
	private final TeamIdentifier teamIdentifier;

	/**
	 * {@link Team} under this {@link TeamManagement}.
	 */
	private final Team team;

	/**
	 * Initiate.
	 * 
	 * @param teamIdentifier
	 *            {@link TeamIdentifier} identifying the {@link Team} under this
	 *            {@link TeamManagement}.
	 * @param team
	 *            {@link Team} under this {@link TeamManagement}.
	 */
	public TeamManagementImpl(TeamIdentifier teamIdentifier, Team team) {
		this.teamIdentifier = teamIdentifier;
		this.team = team;
	}

	/**
	 * Initiate.
	 * 
	 * @param team
	 *            {@link Team} under this {@link TeamManagement}.
	 */
	public TeamManagementImpl(Team team) {
		this(createTeamIdentifier(), team);
	}

	/*
	 * ====================== TeamManagement ================================
	 */

	@Override
	public TeamIdentifier getIdentifier() {
		return this.teamIdentifier;
	}

	@Override
	public Team getTeam() {
		return this.team;
	}

}