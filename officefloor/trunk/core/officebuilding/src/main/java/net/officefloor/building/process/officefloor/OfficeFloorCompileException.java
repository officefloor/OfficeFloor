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
package net.officefloor.building.process.officefloor;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.main.OfficeBuildingMain;

/**
 * Indicates failure to compile the {@link OfficeFloor} for opening within the
 * {@link OfficeBuildingMain}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorCompileException extends Error {

	/**
	 * Initiate.
	 * 
	 * @param message
	 *            Message.
	 * @param cause
	 *            Cause.
	 */
	public OfficeFloorCompileException(String message, Throwable cause) {
		super(message, cause);
	}

}