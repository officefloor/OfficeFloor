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

import net.officefloor.compile.spi.office.OfficeTeam;

/**
 * Responsibilities for auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireResponsibility {

	/**
	 * Dependency type.
	 */
	private final Class<?> dependencyType;

	/**
	 * {@link OfficeTeam} name.
	 */
	private final String officeTeamName;

	/**
	 * Initiate.
	 * 
	 * @param dependencyType
	 *            Dependency type.
	 * @param officeTeamName
	 *            {@link OfficeTeam} name.
	 */
	public AutoWireResponsibility(Class<?> dependencyType, String officeTeamName) {
		this.dependencyType = dependencyType;
		this.officeTeamName = officeTeamName;
	}

	/**
	 * Obtains the dependency type.
	 * 
	 * @return Dependency type.
	 */
	public Class<?> getDependencyType() {
		return this.dependencyType;
	}

	/**
	 * Obtains the corresponding {@link OfficeTeam} name for this
	 * responsibility.
	 * 
	 * @return {@link OfficeTeam} name for this responsibility.
	 */
	public String getOfficeTeamName() {
		return this.officeTeamName;
	}

}