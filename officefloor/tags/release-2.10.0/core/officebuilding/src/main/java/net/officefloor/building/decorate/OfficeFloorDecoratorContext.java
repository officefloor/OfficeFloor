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
package net.officefloor.building.decorate;

import java.io.File;

import net.officefloor.console.OfficeBuilding;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Context for decorating the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorDecoratorContext {

	/**
	 * Obtains the raw class path entry to aid decoration.
	 * 
	 * @return Raw class path entry.
	 */
	String getRawClassPathEntry();

	/**
	 * Includes a resolved class path entry.
	 * 
	 * @param classpathEntry
	 *            Resolved class path entry.
	 */
	void includeResolvedClassPathEntry(String classpathEntry);

	/**
	 * <p>
	 * Creates a {@link File} for decoration.
	 * <p>
	 * This will create the {@link File} within the {@link OfficeFloor}
	 * workspace within the {@link OfficeBuilding}. As within the workspace, it
	 * will be cleaned up on the {@link OfficeFloor} closing.
	 * 
	 * @param identifier
	 *            Identifier added to the file name.
	 * @param extension
	 *            Extension for the {@link File}.
	 * @return Created empty {@link File}.
	 */
	File createWorkspaceFile(String identifier, String extension);

}