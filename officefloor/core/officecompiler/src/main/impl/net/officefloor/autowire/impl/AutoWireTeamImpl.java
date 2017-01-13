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
package net.officefloor.autowire.impl;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.AutoWireTeam;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.frame.api.team.source.TeamSource;

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
	 * {@link AutoWire} instances.
	 */
	private final AutoWire[] autoWiring;

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
	 * @param autoWiring
	 *            {@link AutoWire} instances.
	 */
	public AutoWireTeamImpl(OfficeFloorCompiler compiler, String teamName,
			String teamSourceClassName, PropertyList properties,
			AutoWire... autoWiring) {
		super(compiler, properties);
		this.teamName = teamName;
		this.teamSourceClassName = teamSourceClassName;
		this.autoWiring = autoWiring;
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
	public AutoWire[] getAutoWiring() {
		return this.autoWiring;
	}

}