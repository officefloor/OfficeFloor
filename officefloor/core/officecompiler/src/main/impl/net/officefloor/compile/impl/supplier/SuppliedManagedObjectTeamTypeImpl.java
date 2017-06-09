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

import net.officefloor.compile.supplier.SuppliedManagedObjectTeamType;
import net.officefloor.frame.api.team.Team;

/**
 * {@link SuppliedManagedObjectTeamType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SuppliedManagedObjectTeamTypeImpl implements
		SuppliedManagedObjectTeamType {

	/**
	 * {@link Team} name.
	 */
	private final String teamName;

	/**
	 * Initiate.
	 * 
	 * @param teamName
	 *            {@link Team} name.
	 */
	public SuppliedManagedObjectTeamTypeImpl(String teamName) {
		this.teamName = teamName;
	}

	/*
	 * ====================== SuppliedManagedObjectTeamType ================
	 */

	@Override
	public String getTeamName() {
		return this.teamName;
	}

}