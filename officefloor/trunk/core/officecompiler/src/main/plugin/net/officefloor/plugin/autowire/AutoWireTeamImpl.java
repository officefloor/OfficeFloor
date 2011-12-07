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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Team for auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireTeamImpl extends AutoWirePropertiesImpl implements
		AutoWireTeam {

	/**
	 * Name of the {@link OfficeFloorTeam}.
	 */
	private final String teamName;

	/**
	 * {@link TeamSource} class name.
	 */
	private final String teamSourceClassName;

	/**
	 * {@link AutoWireResponsibility} instances.
	 */
	private final AutoWireResponsibility[] responsibilities;

	/**
	 * Initiate.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param teamName
	 *            Name of the {@link OfficeFloorTeam}.
	 * @param teamSourceClassName
	 *            {@link TeamSource} class name. May be an alias.
	 * @param properties
	 *            {@link PropertyList} for the {@link TeamSource}.
	 * @param responsibilities
	 *            {@link AutoWireResponsibility} instances.
	 */
	public AutoWireTeamImpl(OfficeFloorCompiler compiler, String teamName,
			String teamSourceClassName, PropertyList properties,
			AutoWireResponsibility... responsibilities) {
		super(compiler, properties);
		this.teamName = teamName;
		this.teamSourceClassName = teamSourceClassName;
		this.responsibilities = responsibilities;
	}

	/*
	 * ========================= AutoWireTeam ================================
	 */

	@Override
	public String getTeamName() {
		return this.teamName;
	}

	@Override
	public String getTeamSourceClassName() {
		return this.teamSourceClassName;
	}

	@Override
	public AutoWireResponsibility[] getResponsibilities() {
		return this.responsibilities;
	}

}