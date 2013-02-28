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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.building.command.OfficeFloorCommandParameter;

/**
 * {@link OfficeFloorCommandParameter} allowing multiple paths.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractMultiplePathsOfficeFloorCommandParameter extends
		AbstractOfficeFloorCommandParameter {

	/**
	 * Listing of the paths.
	 */
	private final List<String> paths = new LinkedList<String>();

	/**
	 * Path separator.
	 */
	private final String pathSeparator;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param shortName
	 *            Short name.
	 * @param pathSeparator
	 *            Path separator.
	 * @param description
	 *            Description.
	 */
	public AbstractMultiplePathsOfficeFloorCommandParameter(String name,
			String shortName, String pathSeparator, String description) {
		super(name, shortName, description, true);
		this.pathSeparator = pathSeparator;
	}

	/**
	 * Obtains the listing of paths.
	 * 
	 * @return Listing of paths.
	 */
	protected String[] getPaths() {
		return this.paths.toArray(new String[this.paths.size()]);
	}

	/**
	 * Transforms the value into its multiple paths.
	 * 
	 * @param value
	 *            Value to be transformed.
	 * @param pathSeparator
	 *            Path separator.
	 * @return Multiple path entries in the value.
	 */
	protected static String[] transformValueToPaths(String value,
			String pathSeparator) {

		// Ensure have value
		if (value == null) {
			// No paths
			return new String[0];
		}

		// Split value by path separator
		String[] entries = value.split(pathSeparator);

		// Remove extra spacing around the paths
		for (int i = 0; i < entries.length; i++) {
			entries[i] = entries[i].trim();
		}

		// Return the paths
		return entries;
	}

	/**
	 * Transforms the paths into a value.
	 * 
	 * @param paths
	 *            Paths to be transformed.
	 * @param pathSeparator
	 *            Path separator.
	 * @return Value for the multiple paths. <code>null</code> if no paths.
	 */
	protected static String transformPathsToValue(String[] paths,
			String pathSeparator) {

		// Ensure have paths
		if ((paths == null) || (paths.length == 0)) {
			return null; // no paths
		}

		// Construct the value for the paths
		StringBuilder value = new StringBuilder();
		boolean isFirst = true;
		for (String path : paths) {

			// Provide path separator
			if (!isFirst) {
				value.append(pathSeparator);
			}
			isFirst = false;

			// Append the path
			value.append(path);
		}

		// Return the value
		return value.toString();
	}

	/*
	 * =================== OfficeFloorCommandParameter =================
	 */

	@Override
	public void addValue(String value) {

		// Transform value into paths
		String[] entries = transformValueToPaths(value, this.pathSeparator);

		// Load all paths for value
		for (String entry : entries) {
			this.paths.add(entry);
		}
	}

}