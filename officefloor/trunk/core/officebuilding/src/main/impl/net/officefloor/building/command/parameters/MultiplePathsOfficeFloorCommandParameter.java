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
import java.util.LinkedList;
import java.util.List;

import net.officefloor.building.command.OfficeFloorCommandParameter;

/**
 * {@link OfficeFloorCommandParameter} allowing multiple paths.
 * 
 * @author Daniel Sagenschneider
 */
public class MultiplePathsOfficeFloorCommandParameter extends
		AbstractOfficeFloorCommandParameter {

	/**
	 * Listing of the paths.
	 */
	private final List<String> paths = new LinkedList<String>();

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param shortName
	 *            Short name.
	 */
	public MultiplePathsOfficeFloorCommandParameter(String name,
			String shortName) {
		super(name, shortName);
	}

	/**
	 * Obtains the listing of paths.
	 * 
	 * @return Listing of paths.
	 */
	public String[] getPaths() {
		return this.paths.toArray(new String[this.paths.size()]);
	}

	/*
	 * =================== OfficeFloorCommandParameter =================
	 */

	@Override
	public void addValue(String value) {

		// Split value by path separator as may contain multiple paths
		String[] entries = value.split(File.pathSeparator);

		// Load all paths for value
		for (String entry : entries) {
			this.paths.add(entry);
		}
	}

}