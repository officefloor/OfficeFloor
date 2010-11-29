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
package net.officefloor.building.command.parameters;

import java.io.File;

import net.officefloor.building.command.OfficeFloorCommandParameter;

/**
 * {@link OfficeFloorCommandParameter} for a Jar.
 * 
 * @author Daniel Sagenschneider
 */
public class JarOfficeFloorCommandParameter extends
		AbstractMultiplePathsOfficeFloorCommandParameter {

	/**
	 * Name of {@link OfficeFloorCommandParameter} for the possible archive
	 * locations.
	 */
	public static final String PARAMETER_ARCHIVE_LOCATION = "jar";

	/**
	 * Initiate.
	 */
	public JarOfficeFloorCommandParameter() {
		super(PARAMETER_ARCHIVE_LOCATION, "j", File.pathSeparator,
				"Archive to include on the class path");
	}

	/**
	 * Obtains the listing archives.
	 * 
	 * @return Listing of archives.
	 */
	public String[] getArchives() {
		return this.getPaths();
	}

}