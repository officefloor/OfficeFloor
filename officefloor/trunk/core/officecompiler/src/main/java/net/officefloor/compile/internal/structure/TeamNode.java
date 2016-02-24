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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * {@link OfficeFloorTeam} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamNode extends OfficeFloorTeam, LinkTeamNode {

	/**
	 * Indicates if have the {@link TeamSource} configured.
	 * 
	 * @return <code>true</code> if have the {@link TeamSource} configured.
	 */
	boolean hasTeamSource();

	/**
	 * Loads the type information for this {@link Team}.
	 */
	void loadTeamType();

	/**
	 * Obtains the {@link TeamType} for the {@link TeamSource}.
	 * 
	 * @return {@link TeamType} for the {@link TeamSource}.
	 */
	TeamType getTeamType();

	/**
	 * Loads the {@link OfficeFloorTeamSourceType}.
	 */
	public void loadOfficeFloorTeamSourceType();

	/**
	 * Obtains the {@link OfficeFloorTeamSourceType}.
	 * 
	 * @return {@link OfficeFloorTeamSourceType} or <code>null</code> if issue
	 *         loading with issue reported to the {@link CompilerIssues}.
	 */
	OfficeFloorTeamSourceType getOfficeFloorTeamSourceType();

	/**
	 * Builds the {@link Team} for this {@link TeamNode}.
	 * 
	 * @param builder
	 *            {@link OfficeFloorBuilder}.
	 */
	void buildTeam(OfficeFloorBuilder builder);
}