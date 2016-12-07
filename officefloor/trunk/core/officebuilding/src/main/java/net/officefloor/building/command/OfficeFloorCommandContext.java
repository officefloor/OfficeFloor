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

import net.officefloor.building.process.ManagedProcess;

/**
 * Context for the {@link OfficeFloorCommand} to create a {@link ManagedProcess}
 * to undertake the command.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorCommandContext {

	/**
	 * Provides support for including a class path entry.
	 * 
	 * @param classPathEntry
	 *            Class path entry.
	 */
	void includeClassPathEntry(String classPathEntry);

}