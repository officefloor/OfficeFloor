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
 * {@link OfficeFloorCommandParameter} for the password.
 * 
 * @author Daniel Sagenschneider
 */
public class PasswordOfficeFloorCommandParameter extends
		AbstractSingleValueOfficeFloorCommandParameter {
	
	/**
	 * Default password.
	 */
	public static final String DEFAULT_PASSWORD = "password";

	/**
	 * Initiate.
	 */
	public PasswordOfficeFloorCommandParameter() {
		super("password", "p", "Password");
	}

	/**
	 * Obtains password.
	 * 
	 * @return Password.
	 * @throws IllegalArgumentException
	 *             If no password provided.
	 */
	public String getPassword() throws IllegalArgumentException {

		// Ensure have password
		String password = this.getValue();
		if ((password == null) || (password.trim().length() == 0)) {
			throw new IllegalArgumentException("No password provided");
		}

		// Return the password
		return password;
	}

}