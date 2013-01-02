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
package net.officefloor.building.command.parameters;

import net.officefloor.building.command.OfficeFloorCommandParameter;

/**
 * {@link OfficeFloorCommandParameter} for the user name.
 * 
 * @author Daniel Sagenschneider
 */
public class UsernameOfficeFloorCommandParameter extends
		AbstractSingleValueOfficeFloorCommandParameter {
	
	/**
	 * Default user name.
	 */
	public static final String DEFAULT_USER_NAME = "admin";

	/**
	 * Initiate.
	 */
	public UsernameOfficeFloorCommandParameter() {
		super("username", "u", "User name");
	}

	/**
	 * Obtains user name.
	 * 
	 * @return User name.
	 * @throws IllegalArgumentException
	 *             If no user name provided.
	 */
	public String getUserName() throws IllegalArgumentException {

		// Ensure have user name
		String userName = this.getValue();
		if ((userName == null) || (userName.trim().length() == 0)) {
			throw new IllegalArgumentException("No user name provided");
		}

		// Return the user name
		return userName;
	}

}