/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.building.process;

/**
 * Call back about completion/failure of a command.
 * 
 * @author Daniel Sagenschneider
 */
public interface CommandCallback {

	/**
	 * Flags that the command is complete.
	 * 
	 * @param response
	 *            Response from the command.
	 */
	void complete(Object response);

	/**
	 * Flags that the command failed.
	 * 
	 * @param failure
	 *            Failure of the command.
	 */
	void failed(Throwable failure);

}