/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import net.officefloor.frame.api.execute.Task;

/**
 * Indicates an unknown {@link Task} was requested.
 * 
 * @author Daniel Sagenschneider
 */
public class UnknownTaskException extends Exception {

	/**
	 * Name of the unknown {@link Task}.
	 */
	private final String unknownTaskName;

	/**
	 * Initiate.
	 * 
	 * @param unknownTaskName
	 *            Name of the unknown {@link Task}.
	 */
	public UnknownTaskException(String unknownTaskName) {
		super("Unknown Task '" + unknownTaskName + "'");
		this.unknownTaskName = unknownTaskName;
	}

	/**
	 * Obtains the name of the unknown {@link Task}.
	 * 
	 * @return Name of the unknown {@link Task}.
	 */
	public String getUnknownTaskName() {
		return this.unknownTaskName;
	}
}
