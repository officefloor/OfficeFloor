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
package net.officefloor.building.command.parameters;

import java.io.File;

import net.officefloor.building.command.OfficeFloorCommandParameter;

/**
 * Class path {@link OfficeFloorCommandParameter}.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassPathOfficeFloorCommandParameter extends
		AbstractMultiplePathsOfficeFloorCommandParameter {

	/**
	 * Name of {@link OfficeFloorCommandParameter} for possible class path.
	 */
	public static final String PARAMETER_CLASS_PATH = "classpath";

	/**
	 * Initiate.
	 */
	public ClassPathOfficeFloorCommandParameter() {
		super(PARAMETER_CLASS_PATH, "cp", File.pathSeparator,
				"Raw entry to include on the class path");
	}

	/**
	 * Obtains the class path entries.
	 * 
	 * @return Class path entries.
	 */
	public String[] getClassPathEntries() {
		return this.getPaths();
	}

}