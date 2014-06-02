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
package net.officefloor.frame.api.manage;

import net.officefloor.frame.api.execute.Work;

/**
 * Indicates an unknown {@link Work} was requested.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownWorkException extends Exception {

	/**
	 * Name of the unknown {@link Work}.
	 */
	private final String unknownWorkName;

	/**
	 * Initiate.
	 * 
	 * @param unknownWorkName
	 *            Name of the unknown {@link Work}.
	 */
	public UnknownWorkException(String unknownWorkName) {
		super("Unknown Work '" + unknownWorkName + "'");
		this.unknownWorkName = unknownWorkName;
	}

	/**
	 * Obtains the name of the unknown {@link Work}.
	 * 
	 * @return Name of the unknown {@link Work}.
	 */
	public String getUnknownWorkName() {
		return this.unknownWorkName;
	}
}
