/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.team.Team;

/**
 * Provides management of a particular {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamManagement {

	/**
	 * Obtains the identifier for the {@link Team}.
	 * 
	 * @return Identifier for the {@link Team}.
	 */
	Object getIdentifier();

	/**
	 * Obtains the {@link Team} under this management.
	 * 
	 * @return {@link Team} under this management.
	 */
	Team getTeam();

}