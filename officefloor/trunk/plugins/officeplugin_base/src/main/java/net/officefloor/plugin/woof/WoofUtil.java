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
package net.officefloor.plugin.woof;

/**
 * Utility functions for WoOF.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofUtil {

	/**
	 * Determine if a WoOF resource (by naming).
	 * 
	 * @param fileName
	 *            File name.
	 * @return <code>true</code> if WoOF resource.
	 */
	public static boolean isWoofResource(String fileName) {

		// Strip off to just file name (no directory names)
		int index = fileName.lastIndexOf('/');
		if (index >= 0) {
			fileName = fileName.substring(index + "/".length());
		}

		// Strip off the extension
		index = fileName.lastIndexOf('.');
		if (index < 0) {
			return false; // must have extension for WoOF resource
		}
		fileName = fileName.substring(0, index);

		// Strip off woof marker
		index = fileName.lastIndexOf('.');
		if (index < 0) {
			return false; // must have woof marker in name
		}
		fileName = fileName.substring(index + ".".length());

		// WoOF resource if WoOF marker
		return ("woof".equalsIgnoreCase(fileName));
	}

	/**
	 * All access via static methods.
	 */
	private WoofUtil() {
	}

}