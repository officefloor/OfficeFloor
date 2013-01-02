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
package net.officefloor.autowire.impl.supplier;

import net.officefloor.autowire.AutoWire;
import net.officefloor.autowire.supplier.SuppliedManagedObjectTeamType;
import net.officefloor.frame.spi.team.Team;

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
	 * {@link Team} {@link AutoWire}.
	 */
	private final AutoWire teamAutoWire;

	/**
	 * Initiate.
	 * 
	 * @param teamName
	 *            {@link Team} name.
	 * @param teamAutoWire
	 *            {@link Team} {@link AutoWire}.
	 */
	public SuppliedManagedObjectTeamTypeImpl(String teamName,
			AutoWire teamAutoWire) {
		this.teamName = teamName;
		this.teamAutoWire = teamAutoWire;
	}

	/*
	 * ====================== SuppliedManagedObjectTeamType ================
	 */

	@Override
	public String getTeamName() {
		return this.teamName;
	}

	@Override
	public AutoWire getTeamAutoWire() {
		return this.teamAutoWire;
	}

}