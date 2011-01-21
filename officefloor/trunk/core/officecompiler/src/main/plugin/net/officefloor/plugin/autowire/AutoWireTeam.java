/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.autowire;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Team for auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireTeam extends AutoWireProperties {

	/**
	 * Name of the {@link OfficeFloorTeam}.
	 */
	private final String teamName;

	/**
	 * {@link TeamSource} class.
	 */
	private final Class<? extends TeamSource> teamSourceClass;

	/**
	 * {@link AutoWireResponsibility} instances.
	 */
	private final AutoWireResponsibility[] responsibilities;

	/**
	 * Initiate.
	 * 
	 * @param teamName
	 *            Name of the {@link OfficeFloorTeam}.
	 * @param teamSourceClass
	 *            {@link TeamSource} class.
	 * @param properties
	 *            {@link PropertyList} for the {@link TeamSource}.
	 * @param responsibilities
	 *            {@link AutoWireResponsibility} instances.
	 */
	public AutoWireTeam(String teamName,
			Class<? extends TeamSource> teamSourceClass,
			PropertyList properties, AutoWireResponsibility... responsibilities) {
		super(properties);
		this.teamName = teamName;
		this.teamSourceClass = teamSourceClass;
		this.responsibilities = responsibilities;
	}

	/**
	 * Obtains the name of the {@link OfficeFloorTeam}.
	 * 
	 * @return Name of the {@link OfficeFloorTeam}.
	 */
	public String getTeamName() {
		return this.teamName;
	}

	/**
	 * Obtains the {@link TeamSource} class.
	 * 
	 * @return {@link TeamSource} class.
	 */
	public Class<? extends TeamSource> getTeamSourceClass() {
		return this.teamSourceClass;
	}

	/**
	 * Obtains the {@link AutoWireResponsibility} instances for this
	 * {@link AutoWireTeam}.
	 * 
	 * @return {@link AutoWireResponsibility} instances for this
	 *         {@link AutoWireTeam}.
	 */
	public AutoWireResponsibility[] getResponsibilities() {
		return this.responsibilities;
	}

}