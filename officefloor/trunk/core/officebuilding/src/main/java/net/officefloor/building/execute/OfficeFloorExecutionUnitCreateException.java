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
package net.officefloor.building.execute;

/**
 * Indicates failure to create the {@link OfficeFloorExecutionUnit}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorExecutionUnitCreateException extends Exception {

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Message.
	 */
	public OfficeFloorExecutionUnitCreateException(String message) {
		super(message);
	}

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause.
	 */
	public OfficeFloorExecutionUnitCreateException(String message,
			Throwable cause) {
		super(message, cause);
	}

}