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
package net.officefloor.eclipse.wizard.teamsource;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Instance of a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamInstance {

	/**
	 * Name of this {@link Team}.
	 */
	private final String teamName;

	/**
	 * {@link TeamSource} class name.
	 */
	private final String teamSourceClassName;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link TeamType}.
	 */
	private final TeamType teamType;

	/**
	 * Initiate for public use.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @param teamSourceClassName
	 *            {@link TeamSource} class name.
	 */
	public TeamInstance(String teamName, String teamSourceClassName) {
		this.teamName = teamName;
		this.teamSourceClassName = teamSourceClassName;
		this.propertyList = OfficeFloorCompiler.newPropertyList();
		this.teamType = null;
	}

	/**
	 * Initiate from {@link TeamSourceInstance}.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @param teamSourceClassName
	 *            {@link TeamSource} class name.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param teamType
	 *            {@link TeamType}.
	 */
	TeamInstance(String teamName, String teamSourceClassName,
			PropertyList propertyList, TeamType teamType) {
		this.teamName = teamName;
		this.teamSourceClassName = teamSourceClassName;
		this.propertyList = propertyList;
		this.teamType = teamType;
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
	 * Obtains the {@link TeamSource} class name.
	 * 
	 * @return {@link TeamSource} class name.
	 */
	public String getTeamSourceClassName() {
		return this.teamSourceClassName;
	}

	/**
	 * Obtains the {@link PropertyList}.
	 * 
	 * @return {@link PropertyList}.
	 */
	public PropertyList getPropertylist() {
		return this.propertyList;
	}

	/**
	 * Obtains the {@link TeamType}.
	 * 
	 * @return {@link TeamType} if obtained from {@link TeamSourceInstance} or
	 *         <code>null</code> if initiated by <code>public</code>
	 *         constructor.
	 */
	public TeamType getTeamType() {
		return this.teamType;
	}

}