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
package net.officefloor.building.command;

/**
 * Factory for the creating an {@link OfficeFloorCommand}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCommandFactory {

	/**
	 * Obtains the name of this {@link OfficeFloorCommand}.
	 * 
	 * @return Name of this {@link OfficeFloorCommand}.
	 */
	String getCommandName();

	/**
	 * Creates the {@link OfficeFloorCommand}.
	 * 
	 * @return {@link OfficeFloorCommand}.
	 */
	OfficeFloorCommand createCommand();

}