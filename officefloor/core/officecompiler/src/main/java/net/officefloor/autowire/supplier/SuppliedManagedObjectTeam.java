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

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * Supplied {@link Team} for the {@link SuppliedManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SuppliedManagedObjectTeam {

	/**
	 * Obtains the name of the {@link SuppliedManagedObjectTeam}.
	 * 
	 * @return Name of the {@link SuppliedManagedObjectTeam}.
	 */
	String getTeamName();

	/**
	 * Obtains the {@link TeamSource} {@link Class} name. May be an alias.
	 * 
	 * @return {@link TeamSource} {@link Class} name.
	 */
	String getTeamSourceClassName();

	/**
	 * Obtains the {@link PropertyList} to configure the {@link TeamSource}.
	 * 
	 * @return {@link PropertyList} to configure the {@link TeamSource}.
	 */
	PropertyList getProperties();

}