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

package net.officefloor.frame.impl.construct.team;

import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.ThreadLocalAwareTeam;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Raw {@link Team} meta-data.
 * 
 * @author Daniel Sagenschneider
 */
public class RawTeamMetaData {

	/**
	 * Name of the {@link Team}.
	 */
	private final String teamName;

	/**
	 * {@link TeamManagement}.
	 */
	private final TeamManagement team;

	/**
	 * Flag indicating if a {@link ThreadLocalAwareTeam}.
	 */
	private final boolean isRequireThreadLocalAwareness;

	/**
	 * Initiate.
	 * 
	 * @param teamName                      Name of {@link Team}.
	 * @param team                          {@link TeamManagement}.
	 * @param isRequireThreadLocalAwareness Flag indicating if a
	 *                                      {@link ThreadLocalAwareTeam}.
	 */
	public RawTeamMetaData(String teamName, TeamManagement team, boolean isRequireThreadLocalAwareness) {
		this.teamName = teamName;
		this.team = team;
		this.isRequireThreadLocalAwareness = isRequireThreadLocalAwareness;
	}

	/**
	 * Obtains the name of the {@link Team}.
	 * 
	 * @return Name of the {@link Team}.
	 */
	public String getTeamName() {
		return this.teamName;
	}

	/**
	 * Obtains the {@link TeamManagement} of the {@link Team}.
	 * 
	 * @return {@link TeamManagement} of the {@link Team}.
	 */
	public TeamManagement getTeamManagement() {
		return this.team;
	}

	/**
	 * Indicates if {@link ThreadLocalAwareTeam}.
	 * 
	 * @return {@link ThreadLocalAwareTeam}.
	 */
	public boolean isRequireThreadLocalAwareness() {
		return this.isRequireThreadLocalAwareness;
	}

}
