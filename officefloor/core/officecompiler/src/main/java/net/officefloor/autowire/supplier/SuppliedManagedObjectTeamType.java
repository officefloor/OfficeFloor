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
package net.officefloor.autowire.supplier;

import net.officefloor.autowire.AutoWire;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * <code>Type definition</code> of a {@link Team} required by the Supplied
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SuppliedManagedObjectTeamType {

	/**
	 * Obtains the name to identify requirement of a {@link Team}.
	 * 
	 * @return Name to identify requirement of a {@link Team}.
	 */
	String getTeamName();

	/**
	 * Obtains the {@link AutoWire} to identify the {@link Team}.
	 * 
	 * @return {@link AutoWire} to identify the {@link Team}.
	 */
	AutoWire getTeamAutoWire();

}