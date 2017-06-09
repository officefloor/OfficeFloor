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
package net.officefloor.compile.impl.supplier;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.supplier.SuppliedManagedObjectTeam;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * {@link SuppliedManagedObjectTeam} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SuppliedManagedObjectTeamImpl implements SuppliedManagedObjectTeam {

	/**
	 * Name of the {@link Team}.
	 */
	private final String teamName;

	/**
	 * Name of the {@link TeamSource} {@link Class}.
	 */
	private final String teamSourceClassName;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList properties;

	/**
	 * Initiate.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @param teamSourceClassName
	 *            Name of the {@link TeamSource} {@link Class}.
	 * @param properties
	 *            {@link PropertyList}.
	 */
	public SuppliedManagedObjectTeamImpl(String teamName,
			String teamSourceClassName, PropertyList properties) {
		this.teamName = teamName;
		this.teamSourceClassName = teamSourceClassName;
		this.properties = properties;
	}

	/*
	 * ====================== SuppliedManagedObjectTeam =====================
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
	public PropertyList getProperties() {
		return this.properties;
	}

}