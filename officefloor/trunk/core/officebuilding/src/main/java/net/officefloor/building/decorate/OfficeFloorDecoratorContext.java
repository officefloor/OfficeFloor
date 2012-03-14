/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.building.command.OfficeFloorCommand;
import net.officefloor.building.command.OfficeFloorCommandEnvironment;
import net.officefloor.building.command.OfficeFloorCommandParameter;
import net.officefloor.compile.properties.Property;
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

	/**
	 * Specifies a {@link Property} for the
	 * {@link OfficeFloorCommandEnvironment}.
	 * 
	 * @param name
	 *            {@link Property} name.
	 * @param value
	 *            {@link Property} value.
	 */
	@Deprecated
	// TODO remove in favour of more specific overrides
	void setEnvironmentProperty(String name, String value);

	/**
	 * <p>
	 * Adds an additional {@link OfficeFloorCommandParameter} value.
	 * <p>
	 * This allows for adding multiple values for a single
	 * {@link OfficeFloorCommandParameter}. It also allows for enriching the
	 * {@link OfficeFloorCommand} without enriching the environment.
	 * 
	 * @param parameterName
	 *            Name of the {@link OfficeFloorCommandParameter}.
	 * @param value
	 *            Value.
	 */
	@Deprecated
	// TODO remove in favour of more specific overrides
	void addCommandOption(String parameterName, String value);

}